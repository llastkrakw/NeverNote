package com.llastkrakw.nevernote.views.task.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.databinding.ActivityNoteCalendarViewBinding
import com.llastkrakw.nevernote.databinding.CalendarDayLayoutBinding
import com.llastkrakw.nevernote.databinding.CalendarMonthHeaderLayoutBinding
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFolders
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModelFactory
import com.llastkrakw.nevernote.views.notes.fragments.NoteTimePickerFragment
import com.llastkrakw.nevernote.views.task.fragments.AddTaskFragment
import com.llastkrakw.nevernote.views.task.fragments.AddTaskFragment.Companion.TASK_CONTENT_REMINDER
import com.llastkrakw.nevernote.views.task.fragments.TaskTimerPickerFragment
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

class TaskCalendarView : AppCompatActivity() {

    private lateinit var binding: ActivityNoteCalendarViewBinding
    private lateinit var  calendarView : CalendarView
    private var selectedDate: LocalDate? = null
    private val taskViewModel : TaskViewModel by viewModels {
        TaskViewModelFactory((application as NeverNoteApplication).taskRepository, application as NeverNoteApplication)
    }

    private var taskContent : String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNoteCalendarViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        taskContent = intent?.getStringExtra(TASK_CONTENT_REMINDER)

        calendarView = binding.calendarView

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                // Set the calendar day for this container.
                container.day = day
                val textView = container.textView
                container.textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    if(day.date.toEpochDay() < LocalDate.now().toEpochDay()){
                        textView.alpha = 0.3f
                    }
                    else{
                        container.textView.setTextColor(getColor(R.color.blue))
                        // Show the month dates. Remember that views are recycled!
                        textView.visibility = View.VISIBLE
                        if (day.date == selectedDate) {
                            // If this is the selected date, show a round background and change the text color.
                            textView.setTextColor(getColor(R.color.white))
                            textView.setBackgroundResource(R.drawable.date_selection_background)
                        } else {
                            // If this is NOT the selected date, remove the background and reset the text color.
                            textView.setTextColor(getColor(R.color.blue))
                            textView.background = null
                        }
                    }

                }
                else {
                    // Hide in and out dates
                    //textView.visibility = View.INVISIBLE
                    container.textView.setTextColor(getColor(R.color.search_button_color_light))
                }
            }

        }

        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                container.textView.text = String.format("${
                    month.yearMonth.month.name.lowercase(Locale.getDefault())
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                } ${month.year}")
            }
        }

        val currentMonth = YearMonth.now()
        //val firstMonth = currentMonth.minusMonths(10)
        val lastMonth = currentMonth.plusMonths(10)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        calendarView.setup(currentMonth, lastMonth, firstDayOfWeek)
        calendarView.smoothScrollToMonth(currentMonth)

        binding.validateDate.setOnClickListener {
            if(selectedDate == null){
                Toast.makeText(this, "Choose a date", Toast.LENGTH_LONG).show()
            }
            else{
                supportFragmentManager.let { manager -> taskContent?.let { it1 ->
                    TaskTimerPickerFragment(
                        taskViewModel,
                        it1,
                        selectedDate!!.toEpochDay()
                    ).show(manager, "Task reminder")
                } }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        //val textView: TextView = view.findViewById(R.id.calendarDayText)

        // With ViewBinding
        val textView = CalendarDayLayoutBinding.bind(view).calendarDayText

        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                // Use the CalendarDay associated with this container.
                // Check the day owner as we do not want to select in or out dates.
                if (day.owner == DayOwner.THIS_MONTH && (day.date.toEpochDay() >= LocalDate.now().toEpochDay())) {
                    // Keep a reference to any previous selection
                    // in case we overwrite it and need to reload it.
                    val currentSelection = selectedDate
                    if (currentSelection == day.date) {
                        // If the user clicks the same date, clear selection.
                        selectedDate = null
                        // Reload this date so the dayBinder is called
                        // and we can REMOVE the selection background.
                        calendarView.notifyDateChanged(currentSelection)
                        Log.d("selectedDate", "same clear")
                    } else {
                        selectedDate = day.date
                        // Reload the newly selected date so the dayBinder is
                        // called and we can ADD the selection background.
                        calendarView.notifyDateChanged(day.date)
                        if (currentSelection != null) {
                            // We need to also reload the previously selected
                            // date so we can REMOVE the selection background.
                            calendarView.notifyDateChanged(currentSelection)
                            Log.d("selectedDate", "previous clear")
                        }
                    }

                    Log.d("selectedDate", currentSelection.toString())
                }
            }
        }
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val textView = CalendarMonthHeaderLayoutBinding.bind(view).headerTextView
    }
}


