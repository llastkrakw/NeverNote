package com.llastkrakw.nevernote.feature.task.repositories

import androidx.annotation.WorkerThread
import com.llastkrakw.nevernote.feature.task.datas.database.TaskRoomDataBase
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDatabase : TaskRoomDataBase) {
    val allTask : Flow<List<Task>> = taskDatabase.taskDao().getTasks()


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertTask(task: Task) : Long{
        return taskDatabase.taskDao().insert(task)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteTask(task: Task){
        taskDatabase.taskDao().delete(task)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateTask(task: Task){
        taskDatabase.taskDao().update(task)
    }
}