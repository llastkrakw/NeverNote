package com.llastkrakw.nevernote.views.task.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModelFactory
import com.llastkrakw.nevernote.views.task.activities.TaskCalendarView
import java.util.*

class AddTaskFragment : DialogFragment() {

    private val taskViewModel : TaskViewModel by viewModels {
        TaskViewModelFactory((activity?.application as NeverNoteApplication).taskRepository, activity?.application as NeverNoteApplication)
    }

    companion object{
        const val TASK_CONTENT_REMINDER = "com.llastkrakw.nevernote.task.reminder"
        const val TASK_DIALOG_REMINDER = 15
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val addTaskView = layoutInflater.inflate(R.layout.add_task_layout, null)

        builder.setView(addTaskView)
        val alertDialog = builder.create()
        //alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editText = addTaskView.findViewById<EditText>(R.id.edit_text_add_task)
        val addButton = addTaskView.findViewById<TextView>(R.id.add_task)
        val reminderButton = addTaskView.findViewById<TextView>(R.id.task_reminder)

        reminderButton.setOnClickListener {
            editText.text?.let { str ->
                if (str.toString().isNotEmpty()){
                    //activity?.supportFragmentManager?.let { manager -> TaskTimerPickerFragment(taskViewModel, str.toString(), alertDialog).show(manager, "Task reminder") }
                    val reminderIntent = Intent(context, TaskCalendarView::class.java)
                    reminderIntent.putExtra(TASK_CONTENT_REMINDER, str.toString())
                    startActivity(reminderIntent)
                    alertDialog.cancel()
                }
            }
        }

        addButton.setOnClickListener(){
            editText.text?.let {
                if(it.toString().isNotEmpty()){
                    taskViewModel.insertTask(Task(null, it.toString(), Date(), null, false), null)
                    alertDialog.cancel()
                }
            }
        }

        val wmlp: WindowManager.LayoutParams = alertDialog.window!!.attributes

        wmlp.gravity = Gravity.BOTTOM

        return alertDialog

    }
}