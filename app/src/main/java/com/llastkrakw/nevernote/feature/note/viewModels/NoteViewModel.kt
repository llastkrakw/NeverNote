package com.llastkrakw.nevernote.feature.note.viewModels

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.*
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.constants.getAudioFileContentUri
import com.llastkrakw.nevernote.core.constants.isQPlus
import com.llastkrakw.nevernote.core.extension.*
import com.llastkrakw.nevernote.core.utilities.marshallParcelable
import com.llastkrakw.nevernote.feature.note.datas.entities.*
import com.llastkrakw.nevernote.feature.note.receiver.NoteAlarmReceiver
import com.llastkrakw.nevernote.feature.note.repositories.NoteRepository
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.util.*

class NoteViewModel(private val noteRepository: NoteRepository, private val app: Application) : ViewModel() {

    private val _isGrid = MutableLiveData(true)
    val isGrid: LiveData<Boolean> = _isGrid

    private val _isNoteNext = MutableLiveData(false)
    val isNotNext : LiveData<Boolean> = _isNoteNext

    private val _selectedNotes = MutableLiveData<MutableList<Note>>(mutableListOf())
    val selectedNotes : LiveData<MutableList<Note>> = _selectedNotes

    val allNotesAsc : LiveData<List<Note>> = noteRepository.allNotesAsc.asLiveData()
    val allNotesAscWithFolders : LiveData<List<NoteWithFoldersAndRecords>> = noteRepository.allNotesAscWithFolders.asLiveData()
    val allNotesLastUpdate : LiveData<List<Note>> = noteRepository.allNotesLastUpdate.asLiveData()

    val allFolderWithNotes : LiveData<List<FolderWithNotes>> = noteRepository.allFolderWithNotes.asLiveData()
    val allFolder : LiveData<List<Folder>> = noteRepository.allFolder.asLiveData()

    val allRecordRef : LiveData<List<RecordRef>> = noteRepository.allRecordRef.asLiveData()


    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    var returnedIdWhenAdd : LiveData<Int> = MutableLiveData()

    fun toggleLayoutNoteManager(value: Boolean){
        _isGrid.value = value
    }

    fun toggleIsNoteNext(){
        _isNoteNext.value = !(_isNoteNext.value!!)
    }

    private var list = mutableListOf<NoteWithFoldersAndRecords>()

    init {
        allNotesAscWithFolders.observeForever {
            list = it.toMutableList()
        }
    }

    /*
    *
    * Note
    *
    * */

    fun insertNote(note: Note) = viewModelScope.launch{
        returnedIdWhenAdd = liveData {
            val id = noteRepository.insertNote(note)
            emit(id)
        }
        Log.d("note_update", "note ${note.noteTitle} was added")
        app.toast("note ${note.noteId} was added")
    }


    fun updateNote(note: Note) = viewModelScope.launch {
        Log.d("note_update", note.noteContent)
        noteRepository.updateNote(note)
        app.toast("note ${note.noteId} was update")
    }


    fun deleteNotes() = viewModelScope.launch {
        selectedNotes.value?.let { noteRepository.deleteNotes(it.toList()) }
        _selectedNotes.value?.clear()
        app.toast("Notes was deleted")
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteNote(note)

        app.toast("note ${note.noteId} was deleted")
    }

    fun selectNote(note: Note){
        Log.d("multi", "note selected ${note.noteBg}")
        _selectedNotes.value?.let {
            if(!it.contains(note))
                it.add(note)
            Log.d("multi", "notes ${it.size}")
        }

        app.toast("note ${note.noteId} was selected")
    }

    fun selectAll(){
        allNotesAsc.value?.forEach { note ->
            _selectedNotes.value?.let {
                if(!it.contains(note))
                    it.add(note)
                Log.d("multi", "notes ${it.size}")
            }
        }

        app.toast("All notes selected")
    }

    fun deselectNote(note: Note){
        Log.d("multi", "note deselected ${note.noteBg}")
        _selectedNotes.value?.let {
            if(it.contains(note))
                it.remove(note)
            Log.d("multi", "notes ${it.size}")
        }
        app.toast("note ${note.noteId} was deselected")
    }


    fun retrieveNoteWithFolder(id : Int) : NoteWithFoldersAndRecords? {
        Log.d("go_detail", list.size.toString())
        return list.firstOrNull { noteWithFolders -> noteWithFolders.note.noteId == id }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addNoteReminder(note: NoteWithFoldersAndRecords, selectedTime : Long) = viewModelScope.launch {

        val notifyIntent = Intent(app, NoteAlarmReceiver::class.java)

        val bytes: ByteArray = marshallParcelable(note)

        notifyIntent.putExtra(NOTIFICATION_NOTE_EXTRA, bytes)

        val notifyPendingIntent = note.note.noteId?.let {
            PendingIntent.getBroadcast(
                app,
                it,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

/*        val notificationManager = ContextCompat.getSystemService(
                app,
                NotificationManager::class.java
        ) as NotificationManager
        notificationManager.sendNotification(app, note)*/

        Log.d("timer", selectedTime.toString())
        Log.d("timer", "warr papa")

/*        AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                selectedTime,
                notifyPendingIntent
        )*/

        if (notifyPendingIntent != null) {
            AlarmManagerCompat.setAlarmClock(
                alarmManager,
                selectedTime,
                notifyPendingIntent,
                notifyPendingIntent
            )
        }

        note.note.noteReminder = Date.from(Instant.ofEpochMilli(selectedTime))

        updateNote(note.note)
    }

    /*
    *
    * Folder
    *
    * */


    fun insertFolder(folder: Folder) = viewModelScope.launch {
        noteRepository.insertFolder(folder)
        app.toast("folder ${folder.folderName} was added")
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch {
        noteRepository.updateFolder(folder)
        app.toast("folder ${folder.folderName} was updated")
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        noteRepository.deleteFolder(folder)
        app.toast("folder ${folder.folderName} was deleted")
    }


    fun addNotesToFolder(folder: FolderWithNotes) = viewModelScope.launch {
        selectedNotes.value?.forEach { note ->
            if(note.noteId != null && folder.folder.folderId != null)
                if (!folder.notes.contains(note))
                    noteRepository.insertCrossRef(FolderNoteCrossRef(folder.folder.folderId, note.noteId!!))
        }
        _selectedNotes.value?.clear()

        app.toast("All notes was added to folder ${folder.folder.folderName}")
    }


    /*
    *
    * RecordRef
    *
    * */
    fun insertRecordRef(recordRef: RecordRef) = viewModelScope.launch {
        noteRepository.insertRecordRef(recordRef)
    }

    fun updateRecordRef(recordRef: RecordRef) = viewModelScope.launch {
        noteRepository.updateRecordRef(recordRef)
    }

    fun deleteRecordRef(recordRef: RecordRef) = viewModelScope.launch {
        noteRepository.deleteRecordRef(recordRef)
    }


    fun getRecordings(): ArrayList<Recording> {
        return if (isQPlus()) {
            getMediaStoreRecordings()
        } else {
            getLegacyRecordings()
        }
    }

    @SuppressLint("InlinedApi")
    private fun getMediaStoreRecordings(): ArrayList<Recording> {
        val recordings = ArrayList<Recording>()

        val uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.RELATIVE_PATH
        )

        val selection = "${MediaStore.Audio.Media.OWNER_PACKAGE_NAME} = ?"
        val selectionArgs = arrayOf(app.packageName)
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        app.queryCursor(uri, projection, selection, selectionArgs, sortOrder, true) { cursor ->
            val id = cursor.getIntValue(MediaStore.Audio.Media._ID)
            val title = cursor.getStringValue(MediaStore.Audio.Media.DISPLAY_NAME)
            val timestamp = cursor.getIntValue(MediaStore.Audio.Media.DATE_ADDED)
            var duration = cursor.getLongValue(MediaStore.Audio.Media.DURATION) / 1000
            var size = cursor.getIntValue(MediaStore.Audio.Media.SIZE)
            val relativePath = cursor.getStringValue(MediaStore.Audio.Media.RELATIVE_PATH)

            if (duration == 0L) {
                duration = getDurationFromUri(id.toLong())
            }

            if (size == 0) {
                size = getSizeFromUri(id.toLong())
            }

            val recording = Recording(id, title, relativePath, timestamp, duration.toInt(), size)
            recordings.add(recording)
        }

        return recordings
    }

    private fun getLegacyRecordings(): ArrayList<Recording> {
        val recordings = ArrayList<Recording>()
        val files = File(app.config.saveRecordingsFolder).listFiles() ?: return recordings

        files.filter { it.isAudioFast() }.forEach {
            val id = it.hashCode()
            val title = it.name
            val path = it.absolutePath
            val timestamp = (it.lastModified() / 1000).toInt()
            val duration = app.getDuration(it.absolutePath) ?: 0
            val size = it.length().toInt()
            val recording = Recording(id, title, path, timestamp, duration, size)
            recordings.add(recording)
        }

        recordings.sortByDescending { it.timestamp }
        return recordings
    }

    private fun getDurationFromUri(id: Long): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(app, getAudioFileContentUri(id))
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
            Math.round(time.toLong() / 1000.toDouble())
        } catch (e: Exception) {
            0L
        }
    }

    private fun getSizeFromUri(id: Long): Int {
        val recordingUri = getAudioFileContentUri(id)
        return try {
            app.contentResolver.openInputStream(recordingUri)?.available() ?: 0
        } catch (e: Exception) {
            0
        }
    }


    private fun deleteMediaStoreRecording(recording: Recording) {

        if (isQPlus()) {
            val uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val selection = "${MediaStore.Audio.Media._ID} = ?"
            val selectionArgs = arrayOf(recording.id.toString())
            app.contentResolver.delete(uri, selection, selectionArgs)
        } /*else {
            this.deleteFile(File(recording.path).toFileDirItem(activity))
        }*/
    }

}

class NoteViewModelFactory(private val noteRepository: NoteRepository, private val app: Application) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(noteRepository, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}