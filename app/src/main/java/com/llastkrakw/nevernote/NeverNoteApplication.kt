package com.llastkrakw.nevernote

import android.app.Application
import android.content.Context
import com.llastkrakw.nevernote.core.config.Config
import com.llastkrakw.nevernote.feature.note.datas.database.NoteRoomDatabase
import com.llastkrakw.nevernote.feature.note.repositories.NoteRepository
import com.llastkrakw.nevernote.feature.task.datas.database.TaskRoomDataBase
import com.llastkrakw.nevernote.feature.task.repositories.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class NeverNoteApplication : Application() {

    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val noteDatabase by lazy { NoteRoomDatabase.getDatabase(this, applicationScope) }
    val noteRepository by lazy { NoteRepository(noteDatabase) }

    private val taskDatabase by lazy { TaskRoomDataBase.getDataBase(this, applicationScope) }
    val taskRepository by lazy { TaskRepository(taskDatabase) }

    override fun onCreate() {
        super.onCreate()
/*        UnsplashPhotoPicker.init(
            this,
            BuildConfig.UNSPLASH_ACCESS_KEY,
            BuildConfig.UNSPLASH_SECRET_KEY
            *//* optional page size (number of photos per page) *//*
        )*/

    }
}