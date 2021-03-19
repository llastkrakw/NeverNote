package com.llastkrakw.nevernote.feature.task.adapters

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import java.util.*

class TaskAdapter(private val taskViewModel: TaskViewModel, private val owner: LifecycleOwner) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder.create(parent, taskViewModel, owner)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }


    class TaskViewHolder(itemView : View, private val taskViewModel: TaskViewModel, private val owner: LifecycleOwner) : RecyclerView.ViewHolder(itemView) {

        private val status : CheckBox = itemView.findViewById(R.id.is_complete)
        private val content : TextView = itemView.findViewById(R.id.task_content)

        fun bind(task: Task){
            status.isChecked = task.taskStatus
            if (task.taskStatus){
                val span = SpannableStringBuilder(task.taskContent)
                span.setSpan(StrikethroughSpan(), 0, task.taskContent.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                content.setText(span, TextView.BufferType.SPANNABLE)
            }
            else
                content.text = task.taskContent

            status.setOnCheckedChangeListener { _, isChecked ->
                task.taskStatus = isChecked
                taskViewModel.updateTask(task)
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
                if (item.taskContent.toLowerCase(Locale.ROOT).contains(constraint.toString().toLowerCase(Locale.ROOT))) {
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