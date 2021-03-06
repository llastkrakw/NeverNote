package com.llastkrakw.nevernote.views.notes.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.llastkrakw.nevernote.core.constants.SUCCESS_SONG
import com.llastkrakw.nevernote.core.extension.playUiSong
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFoldersAndRecords
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


class NoteTimePickerFragment(private val noteViewModel: NoteViewModel, private val note : NoteWithFoldersAndRecords, private val date: Long) : DialogFragment(), TimePickerDialog.OnTimeSetListener {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, is24HourFormat(activity))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        //val systemTime = SystemClock.elapsedRealtime()
        val instant = Instant.now() //can be LocalDateTime

        val systemZone = ZoneId.systemDefault() // my timezone

        val currentOffsetForMyZone = systemZone.rules.getOffset(instant)
        Log.d("timer", date.toString())
        val reminder = LocalDate.ofEpochDay(date).atStartOfDay().plusMinutes(minute.toLong()).plusHours(hourOfDay.toLong()).toInstant(currentOffsetForMyZone).toEpochMilli()
        noteViewModel.addNoteReminder(note,  Date.from(Instant.ofEpochMilli(reminder)).time)
        context?.playUiSong(SUCCESS_SONG)
        Log.d("timer", reminder.toString())
        Log.d("timer", Instant.now().toEpochMilli().toString())
        Log.d("timer", "result : ${reminder - System.currentTimeMillis()}")
        Toast.makeText(context, "Reminder was add to : ${Instant.ofEpochMilli(reminder)}", Toast.LENGTH_LONG).show()
        activity?.onBackPressed()
/*        val mainIntent = Intent(context, MainActivity::class.java)
        startActivity(mainIntent)*/
    }


}