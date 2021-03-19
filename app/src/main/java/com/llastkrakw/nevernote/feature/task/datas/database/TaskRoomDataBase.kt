package com.llastkrakw.nevernote.feature.task.datas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.llastkrakw.nevernote.core.constants.TASK_DATABASE
import com.llastkrakw.nevernote.core.converters.DateConverter
import com.llastkrakw.nevernote.feature.task.datas.dao.TaskDao
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class TaskRoomDataBase : RoomDatabase() {

    abstract fun taskDao() : TaskDao

    companion object{

        @Volatile
        var INSTANCE : TaskRoomDataBase? = null

        fun getDataBase(context: Context, scope: CoroutineScope) : TaskRoomDataBase{

            return INSTANCE ?: synchronized(this){

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskRoomDataBase::class.java,
                    TASK_DATABASE
                )
                    .addCallback(TaskDataBaseCallback(scope))
                    .build()

                INSTANCE = instance

                instance
            }

        }

    }

    private class TaskDataBaseCallback(
        private val scope: CoroutineScope
        ) : RoomDatabase.Callback(){


        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.taskDao())
                }
            }
        }

        suspend fun populateDatabase(taskDao: TaskDao){

            val task1 = Task(null, "Welcome to Task !", Date(), null, false)
            taskDao.insert(task1)

            val task2 = Task(null, "Take risk !", Date(), null, false)
            taskDao.insert(task2)

            val task3 = Task(null, "This task is completed", Date(), null, true)
            taskDao.insert(task3)

        }
    }

}