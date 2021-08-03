package com.llastkrakw.nevernote.views.notes.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.*
import com.llastkrakw.nevernote.core.extension.getColoredDrawable
import com.llastkrakw.nevernote.core.extension.getFormattedDuration
import com.llastkrakw.nevernote.feature.note.events.Events
import com.llastkrakw.nevernote.feature.note.services.RecorderService
import com.visualizer.amplitude.AudioRecordView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class RecordDialogFragment : DialogFragment() {

    private lateinit var cancelButton : TextView
    private lateinit var finishButton : TextView
    private lateinit var recordingDuration : TextView
    private lateinit var recordToggle : ImageButton
    private lateinit var audioRecordView: AudioRecordView

    private var status = RECORDING_STOPPED
    private var pauseBlinkTimer = Timer()
    private var bus: EventBus? = null

    private var noteId : Int? = null

    companion object {
        const val TAG = "RecordDialog"
        const val NOTE_ID_FOR_SERVICE = "note_id_for_service"
        const val IS_CANCEL = "is_Cancel"
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val recordView = layoutInflater.inflate(R.layout.recording_layout, null)

        noteId = arguments?.getInt(NOTE_ID_FOR_SERVICE)

        builder.setView(recordView)
        val alertDialog = builder.create()

        val wmlp: WindowManager.LayoutParams = alertDialog.window!!.attributes

        wmlp.gravity = Gravity.BOTTOM

        cancelButton = recordView.findViewById(R.id.record_cancel)
        finishButton = recordView.findViewById(R.id.record_finish)
        recordingDuration = recordView.findViewById(R.id.record_duration)
        recordToggle = recordView.findViewById(R.id.record_toggle)
        audioRecordView = recordView.findViewById(R.id.audioRecordView)

        audioRecordView.recreate()

        updateRecordingDuration(0)
        recordToggle.setOnClickListener {
            if (status == RECORDING_RUNNING || status == RECORDING_PAUSED)
                toggleRecording()
            else{
                startRecording()
                recordToggle.setImageDrawable(getToggleButtonIcon())
            }
        }

        Intent(context, RecorderService::class.java).apply {
            action = GET_RECORDER_INFO
            try {
                requireContext().startService(this)
            } catch (e: Exception) {
            }
        }


        cancelButton.setOnClickListener {
            stopRecording(true)
            status = RECORDING_STOPPED
            alertDialog.cancel()
        }

        finishButton.setOnClickListener {
            stopRecording(false)
            status = RECORDING_STOPPED
            alertDialog.cancel()
        }

        return  alertDialog
    }

    override fun onStart() {
        super.onStart()
        bus = EventBus.getDefault()
        bus!!.register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording(true)
        pauseBlinkTimer.cancel()
    }



    override fun onStop() {
        super.onStop()
        bus?.unregister(this)
    }

    private fun updateRecordingDuration(duration: Int) {
        recordingDuration.text = duration.getFormattedDuration()
    }

    private fun getToggleButtonIcon(): Drawable {
        val drawable = if (status == RECORDING_RUNNING || status == RECORDING_PAUSED) R.drawable.ic_pause else R.drawable.ic_microphone_shape
        return resources.getColoredDrawable(drawableId = drawable, colorId = R.color.white, context = requireContext())
    }

    private fun toggleRecording() {
        status = if (status == RECORDING_RUNNING) {
            RECORDING_PAUSED
        } else {
            RECORDING_RUNNING
        }

        recordToggle.setImageDrawable(getToggleButtonIcon())

        if (status == RECORDING_RUNNING) {
            startRecording()
        } else {
            pauseRecording()
        }
    }

    private fun startRecording() {
        Intent(requireContext(), RecorderService::class.java).apply {
            putExtra(NOTE_ID_FOR_SERVICE, noteId)
            requireContext().startService(this)
        }
        audioRecordView.recreate()
    }

    private fun pauseRecording(){
        Intent(context, RecorderService::class.java).apply {
            action = TOGGLE_PAUSE
            requireContext().startService(this)
        }
    }

    private fun stopRecording(isCancel : Boolean) {
        Log.d("record", "is cancel in fragment $isCancel")
        if(isCancel)
            Intent(requireContext(), RecorderService::class.java).apply {
                action = CANCEL_RECORDING
                putExtra(IS_CANCEL, isCancel)
                requireContext().startService(this)
            }
        else
            Intent(requireContext(), RecorderService::class.java).apply {
                requireContext().stopService(this)
            }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun gotDurationEvent(event: Events.RecordingDuration) {
        updateRecordingDuration(event.duration)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun gotStatusEvent(event: Events.RecordingStatus) {
        status = event.status
        Log.d("record", status.toString())
        recordToggle.setImageDrawable(getToggleButtonIcon())
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun gotAmplitudeEvent(event: Events.RecordingAmplitude) {
        val amplitude = event.amplitude
        if (status == RECORDING_RUNNING) {
            audioRecordView.update(amplitude)
        }
    }
}