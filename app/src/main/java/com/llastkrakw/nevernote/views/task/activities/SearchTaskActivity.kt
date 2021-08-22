package com.llastkrakw.nevernote.views.task.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.BACK_SONG
import com.llastkrakw.nevernote.core.constants.DELETE_SONG
import com.llastkrakw.nevernote.core.extension.playUiSong
import com.llastkrakw.nevernote.core.utilities.SwipeCallback
import com.llastkrakw.nevernote.databinding.ActivitySearchTaskBinding
import com.llastkrakw.nevernote.feature.task.adapters.TaskAdapter
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModelFactory

class SearchTaskActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySearchTaskBinding
    private val taskViewModel : TaskViewModel by viewModels {
        TaskViewModelFactory((application as NeverNoteApplication).taskRepository, application as NeverNoteApplication)
    }

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)


        taskAdapter = TaskAdapter(taskViewModel, this)

        binding.apply {

            cancelSearchTask.setOnClickListener {
                this@SearchTaskActivity.playUiSong(BACK_SONG)
                onBackPressed()
            }

            taskRecycler.layoutManager = LinearLayoutManager(this@SearchTaskActivity)
            taskRecycler.adapter = taskAdapter

            taskViewModel.allTask.observe(this@SearchTaskActivity, Observer {
                taskAdapter.submitList(it)
            })

            editTextSearchTask.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    addRightCancelDrawable(editTextSearchTask)
                }

                override fun afterTextChanged(s: Editable?) {
                    taskViewModel.allTask.value?.let {
                        taskAdapter.performFiltering(s, it)
                    }
                }

            })

            editTextSearchTask.onRightDrawableClicked {
                it.text.clear()
                it.compoundDrawables[2] = null
            }

            val swipeCompleteCallback = object : SwipeCallback(this@SearchTaskActivity){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    this@SearchTaskActivity.playUiSong(DELETE_SONG)
                    val position = viewHolder.absoluteAdapterPosition
                    val task = taskAdapter.currentList[position]
                    taskViewModel.deleteTask(task)
                }
            }

            val completeTouch  = ItemTouchHelper(swipeCompleteCallback)

            completeTouch.attachToRecyclerView(taskRecycler)

        }
    }

    private fun addRightCancelDrawable(editText: EditText) {
        val cancel = ContextCompat.getDrawable(this, R.drawable.ic_cancel)
        cancel?.setBounds(0,0, cancel.intrinsicWidth, cancel.intrinsicHeight)
        editText.setCompoundDrawables(null, null, cancel, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
        this.setOnTouchListener { v, event ->
            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        performClick()
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
    }
}