package com.llastkrakw.nevernote.views.task.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class TaskTimerPickerFragment(private val taskViewModel: TaskViewModel, private val taskContent : String, private val selectedDate: Long) : DialogFragment(), TimePickerDialog.OnTimeSetListener  {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        //val systemTime = SystemClock.elapsedRealtime()
        val instant = Instant.now() //can be LocalDateTime

        val systemZone = ZoneId.systemDefault() // my timezone

        val currentOffsetForMyZone = systemZone.rules.getOffset(instant)
        Log.d("timer", selectedDate.toString())
        val reminder = LocalDate.ofEpochDay(selectedDate).atStartOfDay().plusMinutes(minute.toLong()).plusHours(hourOfDay.toLong()).toInstant(currentOffsetForMyZone).toEpochMilli()
        taskViewModel.insertTask(Task(null, taskContent, Date(), null,false),  Date.from(Instant.ofEpochMilli(reminder)).time)
        activity?.onBackPressed()
    }


}