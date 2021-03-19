package com.llastkrakw.nevernote.feature.note.repositories

import androidx.annotation.WorkerThread
import androidx.room.withTransaction
import com.llastkrakw.nevernote.feature.note.datas.dao.CrossRefDao
import com.llastkrakw.nevernote.feature.note.datas.dao.FolderDao
import com.llastkrakw.nevernote.feature.note.datas.dao.NoteDao
import com.llastkrakw.nevernote.feature.note.datas.database.NoteRoomDatabase
import com.llastkrakw.nevernote.feature.note.datas.entities.*
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.suspendCoroutine

class NoteRepository(private val noteDataBase: NoteRoomDatabase) {

    val allNotesAsc : Flow<List<Note>> = noteDataBase.noteDao().getAlphabetizedNotes()
    val allNotesAscWithFolders : Flow<List<NoteWithFolders>> = noteDataBase.noteDao().getAlphabetizedNotesWithFolders()
    val allNotesLastUpdate : Flow<List<Note>> = noteDataBase.noteDao().getLastUpdateNotes()

    val allFolderWithNotes : Flow<List<FolderWithNotes>> = noteDataBase.folderDao().getAllFolderWithNote()
    val allFolder : Flow<List<Folder>> = noteDataBase.folderDao().getAllFolder()



    /*
    *
    * Note
    *
    * */

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNote(note: Note) {
        noteDataBase.noteDao().insert(note)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateNote(note: Note) {
        noteDataBase.noteDao().update(note)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateNote(notes: List<Note>) {
        noteDataBase.noteDao().update(notes)
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteNotes(notes: List<Note>) {
        noteDataBase.noteDao().delete(notes)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteNote(note: Note) {
        noteDataBase.noteDao().delete(note)
    }

    /*
    *
    * Folder
    *
    * */

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertFolder(folder: Folder) {
        noteDataBase.folderDao().insert(folder)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateFolder(folder: Folder) {
        noteDataBase.folderDao().update(folder)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteFolder(folder: Folder) {
        noteDataBase.folderDao().delete(folder)
    }

    /*
    *
    * CrossRef
    *
    * */

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertCrossRef(crossRef: FolderNoteCrossRef) {
        noteDataBase.crossRefDao().insert(crossRef)
    }

}