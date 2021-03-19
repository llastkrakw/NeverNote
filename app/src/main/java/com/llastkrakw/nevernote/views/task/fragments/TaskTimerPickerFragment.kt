package com.llastkrakw.nevernote.views.task.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import java.util.*

class TaskTimerPickerFragment(private val taskViewModel: TaskViewModel, private val taskContent : String, private val addDialog: Dialog) : DialogFragment(), TimePickerDialog.OnTimeSetListener  {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val systemTime = SystemClock.elapsedRealtime()
        val c = Calendar.getInstance()
        val currentHour = c.get(Calendar.HOUR_OF_DAY)
        val currentMinute = c.get(Calendar.MINUTE)
        val currentSecond = c.get(Calendar.SECOND)
        //Log.d("timer", "time of reminder ${(((hourOfDay * 3600) + (minute * 60)) - ((currentHour * 3600) + (currentMinute * 60)))} ")
        taskViewModel.insertTask(Task(null, taskContent, Date(), null,false),  systemTime + ((((hourOfDay * 3600) + (minute * 60) - currentSecond) - ((currentHour * 3600) + (currentMinute * 60))) * 1000))
        addDialog.cancel()
    }
}