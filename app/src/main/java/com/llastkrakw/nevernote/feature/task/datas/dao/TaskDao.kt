package com.llastkrakw.nevernote.feature.task.datas.dao

import androidx.room.*
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM TABLE_TASK")
    fun getTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task) : Long

    @Delete
    suspend fun delete(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE, entity = Task::class)
    suspend fun update(task: Task)

}