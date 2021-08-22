package com.llastkrakw.nevernote.feature.task.adapters

import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.DESELECTION_SONG
import com.llastkrakw.nevernote.core.constants.SELECTION_SONG
import com.llastkrakw.nevernote.core.extension.dateExpired
import com.llastkrakw.nevernote.core.extension.playUiSong
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import java.util.*

class TaskAdapter(private val taskViewModel: TaskViewModel, private val owner: LifecycleOwner) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder.create(parent, taskViewModel, owner)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }


    class TaskViewHolder(itemView : View, private val taskViewModel: TaskViewModel, private val owner: LifecycleOwner) : RecyclerView.ViewHolder(itemView) {

        private val status : CheckBox = itemView.findViewById(R.id.is_complete)
        private val content : TextView = itemView.findViewById(R.id.task_content)
        private val haveClock: ImageButton = itemView.findViewById(R.id.have_clock)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(task: Task){
            status.isChecked = task.taskStatus
            if (task.taskStatus){
                val span = SpannableStringBuilder(task.taskContent)
                span.setSpan(StrikethroughSpan(), 0, task.taskContent.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                content.setText(span, TextView.BufferType.SPANNABLE)
            }
            else
                content.text = task.taskContent

            haveClock.visibility = if(task.taskReminder != null && !task.taskReminder!!.dateExpired()) View.VISIBLE else View.GONE

            status.setOnClickListener {
                (it as CheckBox).isChecked.let { isChecked ->
                    if (isChecked)
                        itemView.context.playUiSong(SELECTION_SONG)
                    else
                        itemView.context.playUiSong(DESELECTION_SONG)
                    task.taskStatus = isChecked
                    taskViewModel.updateTask(task)
                }
            }
        }

        companion object{
            fun create(parent : ViewGroup, taskViewModel: TaskViewModel, owner: LifecycleOwner) : TaskViewHolder{
                val view : View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.task_item, parent, false)
                return TaskViewHolder(view, taskViewModel, owner)
            }
        }

    }

    fun performFiltering(constraint: CharSequence?, completeList : List<Task>){

        val filteredList = mutableListOf<Task>()

        if (constraint == null || constraint.isEmpty()) {
            submitList(completeList)
        }
        else {
            for (item in completeList) {
                if (item.taskContent.lowercase(Locale.ROOT).contains(constraint.toString()
                        .lowercase(Locale.ROOT))) {
                    filteredList.add(item)
                }
            }

            submitList(filteredList)
        }
    }

    class TaskComparator : DiffUtil.ItemCallback<Task>(){
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.taskId == newItem.taskId
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.taskContent == newItem.taskContent
        }

    }

}