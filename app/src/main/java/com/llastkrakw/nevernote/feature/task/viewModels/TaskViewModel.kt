package com.llastkrakw.nevernote.feature.task.viewModels

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.*
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_TASK_EXTRA
import com.llastkrakw.nevernote.core.extension.toast
import com.llastkrakw.nevernote.core.utilities.marshallParcelable
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.receiver.TaskAlarmReceiver
import com.llastkrakw.nevernote.feature.task.repositories.TaskRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

class TaskViewModel(private val taskRepository: TaskRepository, private val app : Application) : ViewModel() {


    val allTask : LiveData<List<Task>> = taskRepository.allTask.asLiveData()

    private val _completedListSize = MutableLiveData(0)
    var completedListSize : LiveData<Int> = _completedListSize

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    init {
        viewModelScope.launch {
            allTask.observeForever {
                _completedListSize.value = it.filter { task ->
                    task.taskStatus
                }.size
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertTask(task: Task, selectedTime: Long?) = viewModelScope.launch {
        task.taskId = taskRepository.insertTask(task)
        if (selectedTime != null)
            addTaskReminder(task, selectedTime)
        app.toast("task ${task.taskContent} was added")
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.deleteTask(task)
        app.toast("task ${task.taskContent} was deleted")
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTaskReminder(task: Task, selectedTime : Long) = viewModelScope.launch {

        val notifyIntent = Intent(app, TaskAlarmReceiver::class.java)

        val bytes: ByteArray = marshallParcelable(task)

        notifyIntent.putExtra(NOTIFICATION_TASK_EXTRA, bytes)

        val notifyPendingIntent = task.taskId?.let {
            PendingIntent.getBroadcast(
                app,
                it.toInt(),
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

/*        val notificationManager = ContextCompat.getSystemService(
                app,
                NotificationManager::class.java
        ) as NotificationManager
        notificationManager.sendNotification(app, note)*/

        if (notifyPendingIntent != null) {
            AlarmManagerCompat.setAlarmClock(
                alarmManager,
                selectedTime,
                notifyPendingIntent,
                notifyPendingIntent
            )
        }

        task.taskReminder = Date.from(Instant.ofEpochMilli(selectedTime))

        updateTask(task)
    }


}

class TaskViewModelFactory(private val taskRepository: TaskRepository, private val app: Application) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskRepository, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}