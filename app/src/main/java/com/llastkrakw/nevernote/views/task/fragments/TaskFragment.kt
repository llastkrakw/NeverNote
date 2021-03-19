package com.llastkrakw.nevernote.views.task.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.utilities.SwipeCallback
import com.llastkrakw.nevernote.databinding.FragmentTaskBinding
import com.llastkrakw.nevernote.feature.task.adapters.TaskAdapter
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModel
import com.llastkrakw.nevernote.feature.task.viewModels.TaskViewModelFactory
import com.llastkrakw.nevernote.views.task.activities.SearchTaskActivity

class TaskFragment : Fragment() {

    private var _binding : FragmentTaskBinding? = null
    private val binding get()  = _binding!!

    private val taskViewModel : TaskViewModel by viewModels {
        TaskViewModelFactory((activity?.application as NeverNoteApplication).taskRepository, activity?.application as NeverNoteApplication)
    }

    private lateinit var incompleteTaskAdapter : TaskAdapter
    private lateinit var completeTaskAdapter  : TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding =  FragmentTaskBinding.inflate(layoutInflater, container, false)
        createChannel(
                getString(R.string.task_channel_id),
                getString(R.string.task_channel_name)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        context?.let {
            binding.apply {
                viewModel = taskViewModel
                lifecycleOwner = viewLifecycleOwner
                completeTaskAdapter = TaskAdapter(taskViewModel, viewLifecycleOwner)
                incompleteTaskAdapter = TaskAdapter(taskViewModel, viewLifecycleOwner)

                taskRecycler.layoutManager = LinearLayoutManager(context)
                completedTaskRecycler.layoutManager = LinearLayoutManager(context)

                taskRecycler.adapter = incompleteTaskAdapter
                completedTaskRecycler.adapter = completeTaskAdapter


                taskViewModel.allTask.observe(viewLifecycleOwner, Observer { taskList ->

                    taskList.filter {
                        !it.taskStatus
                    }.let { incompleteList ->
                        incompleteTaskAdapter.submitList(incompleteList)
                    }

                    taskList.filter {
                        it.taskStatus
                    }.let {
                        completeList ->
                        completeTaskAdapter.submitList(completeList)
                    }

                })


                searchTask.setOnClickListener {
                    val searchIntent = Intent(context, SearchTaskActivity::class.java)
                    context?.startActivity(searchIntent)
                }

                val swipeIncompleteCallback = object : SwipeCallback(it){
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position = viewHolder.adapterPosition
                        val task = incompleteTaskAdapter.currentList[position]
                        taskViewModel.deleteTask(task)
                    }
                }

                val swipeCompleteCallback = object : SwipeCallback(it){
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val position = viewHolder.adapterPosition
                        val task = completeTaskAdapter.currentList[position]
                        taskViewModel.deleteTask(task)
                    }
                }

                val  incompleteTouch  = ItemTouchHelper(swipeIncompleteCallback)
                val  completeTouch  = ItemTouchHelper(swipeCompleteCallback)

                incompleteTouch.attachToRecyclerView(taskRecycler)
                completeTouch.attachToRecyclerView(completedTaskRecycler)
            }
        }

    }

    private fun createChannel(channelId: String, channelName: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Reminder Task"

            val notificationManager = requireActivity().getSystemService(
                    NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }

    companion object {

        @JvmStatic
        fun newInstance() = TaskFragment()
    }
}