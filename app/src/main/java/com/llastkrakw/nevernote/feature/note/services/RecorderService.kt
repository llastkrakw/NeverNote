package com.llastkrakw.nevernote.feature.note.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.widget.Toast
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.*
import com.llastkrakw.nevernote.core.extension.*
import com.llastkrakw.nevernote.feature.note.datas.entities.RecordRef
import com.llastkrakw.nevernote.feature.note.events.Events
import com.llastkrakw.nevernote.feature.note.repositories.NoteRepository
import com.llastkrakw.nevernote.views.notes.fragments.RecordDialogFragment.Companion.IS_CANCEL
import com.llastkrakw.nevernote.views.notes.fragments.RecordDialogFragment.Companion.NOTE_ID_FOR_SERVICE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*


class RecorderService : Service(), MediaScannerConnection.MediaScannerConnectionClient {
    private val AMPLITUDE_UPDATE_MS = 75L

    private var currFilePath = ""
    private var duration = 0
    private var status = RECORDING_STOPPED
    private var durationTimer = Timer()
    private var amplitudeTimer = Timer()
    private var recorder: MediaRecorder? = null
    private lateinit var mediaScanner :  MediaScannerConnection

    private val noteRepository : NoteRepository by lazy {
        (application as NeverNoteApplication).noteRepository
    }

    private var noteId : Int? = null
    private var isCancel : Boolean? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        noteId = intent.getIntExtra(NOTE_ID_FOR_SERVICE, 100000)
        isCancel = intent.getBooleanExtra(IS_CANCEL, false)

        when (intent.action) {
            GET_RECORDER_INFO -> broadcastRecorderInfo()
            STOP_AMPLITUDE_UPDATE -> amplitudeTimer.cancel()
            TOGGLE_PAUSE -> togglePause()
            CANCEL_RECORDING -> stopRecording(isCancel!!)
            else -> startRecording()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording(false)
    }

    // mp4 output format with aac encoding should produce good enough m4a files according to https://stackoverflow.com/a/33054794/1967672
    private fun startRecording() {
        val baseFolder = if (isQPlus()) {
            cacheDir
        } else {
            val defaultFolder = File(config.saveRecordingsFolder)
            if (!defaultFolder.exists()) {
                defaultFolder.mkdir()
            }

            defaultFolder.absolutePath
        }

        currFilePath = "$baseFolder/${getCurrentFormattedDateTime()}.${config.getExtensionText()}"
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)

            try {
                if (!isQPlus() && isPathOnSD(currFilePath)) {
                    var document = getDocumentFile(currFilePath.getParentPath())
                    document = document?.createFile("", currFilePath.getFilenameFromPath())

                    val outputFileDescriptor = contentResolver.openFileDescriptor(document!!.uri, "w")!!.fileDescriptor
                    setOutputFile(outputFileDescriptor)
                } else {
                    setOutputFile(currFilePath)
                }

                prepare()
                start()
                duration = 0
                status = RECORDING_RUNNING
                broadcastRecorderInfo()

                durationTimer = Timer()
                durationTimer.scheduleAtFixedRate(getDurationUpdateTask(), 1000, 1000)

                startAmplitudeUpdates()
            } catch (e: Exception) {
                showErrorToast(e)
                stopRecording(true)
            }
        }
    }

    private fun stopRecording(isCancel : Boolean) {
        durationTimer.cancel()
        amplitudeTimer.cancel()
        status = RECORDING_STOPPED

        recorder?.apply {
            try {
                stop()
                reset()
                release()

                if (!isCancel)
                    ensureBackgroundThread {
                        if (isQPlus()) {
                            addFileInNewMediaStore()
                        } else {
                            addFileInLegacyMediaStore()
                        }
                        EventBus.getDefault().post(Events.RecordingCompleted())
                    }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }

        recorder = null
    }

    private fun broadcastRecorderInfo() {
        broadcastDuration()
        broadcastStatus()
        startAmplitudeUpdates()
    }

    private fun startAmplitudeUpdates() {
        amplitudeTimer.cancel()
        amplitudeTimer = Timer()
        amplitudeTimer.scheduleAtFixedRate(getAmplitudeUpdateTask(), 0, AMPLITUDE_UPDATE_MS)
    }

    @SuppressLint("NewApi")
    private fun togglePause() {
        try {
            if (status == RECORDING_RUNNING) {
                recorder?.pause()
                status = RECORDING_PAUSED
            } else if (status == RECORDING_PAUSED) {
                recorder?.resume()
                status = RECORDING_RUNNING
            }
            broadcastStatus()
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    @SuppressLint("InlinedApi")
    private fun addFileInNewMediaStore() {
        val audioCollection = Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val storeFilename = currFilePath.getFilenameFromPath()

        val newSongDetails = ContentValues().apply {
            put(Media.DISPLAY_NAME, storeFilename)
            put(Media.TITLE, storeFilename)
            put(Media.MIME_TYPE, storeFilename.getMimeType())
            put(Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/Recordings")
        }

        val newUri = contentResolver.insert(audioCollection, newSongDetails)
        if (newUri == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        try {
            val outputStream = contentResolver.openOutputStream(newUri)
            val inputStream = getFileInputStreamSync(currFilePath)
            inputStream!!.copyTo(outputStream!!, DEFAULT_BUFFER_SIZE)
            GlobalScope.launch(Dispatchers.IO) {
                recordingSavedSuccessfully(true)
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    private fun addFileInLegacyMediaStore() {
        mediaScanner = MediaScannerConnection(applicationContext, this)
        mediaScanner.connect()
    }

    private suspend fun recordingSavedSuccessfully(showFilenameOnly: Boolean) {
        val title = if (showFilenameOnly) currFilePath.getFilenameFromPath() else currFilePath
        val msg = String.format(getString(R.string.recording_saved_successfully), title)
        val newRecordRef = noteId?.let { RecordRef(null, currFilePath, it, title) }
        if (newRecordRef != null) {
            noteRepository.insertRecordRef(newRecordRef)
        }
        playUiSong(SUCCESS_SONG)
        toast(msg, Toast.LENGTH_LONG)
    }

    private fun getDurationUpdateTask() = object : TimerTask() {
        override fun run() {
            if (status == RECORDING_RUNNING) {
                duration++
                broadcastDuration()
            }
        }
    }

    private fun getAmplitudeUpdateTask() = object : TimerTask() {
        override fun run() {
            if (recorder != null) {
                try {
                    EventBus.getDefault().post(Events.RecordingAmplitude(recorder!!.maxAmplitude))
                } catch (ignored: Exception) {
                }
            }
        }
    }


    private fun broadcastDuration() {
        EventBus.getDefault().post(Events.RecordingDuration(duration))
    }

    private fun broadcastStatus() {
        EventBus.getDefault().post(Events.RecordingStatus(status))
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        GlobalScope.launch(Dispatchers.IO) {
            recordingSavedSuccessfully(true)
        }
    }

    override fun onMediaScannerConnected() {
        mediaScanner.scanFile(
            currFilePath,
            currFilePath.getMimeType()
        )
    }

}
