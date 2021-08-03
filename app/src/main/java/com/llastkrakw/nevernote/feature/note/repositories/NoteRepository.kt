package com.llastkrakw.nevernote.feature.note.repositories

import android.util.Log
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.llastkrakw.nevernote.core.extension.toast
import com.llastkrakw.nevernote.feature.note.datas.database.NoteRoomDatabase
import com.llastkrakw.nevernote.feature.note.datas.entities.*
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDataBase: NoteRoomDatabase) {

    val allNotesAsc : Flow<List<Note>> = noteDataBase.noteDao().getAlphabetizedNotes()
    val allNotesAscWithFolders : Flow<List<NoteWithFoldersAndRecords>> = noteDataBase.noteDao().getAlphabetizedNotesWithFoldersAndRecords()
    val allNotesLastUpdate : Flow<List<Note>> = noteDataBase.noteDao().getLastUpdateNotes()

    val allFolderWithNotes : Flow<List<FolderWithNotes>> = noteDataBase.folderDao().getAllFolderWithNote()
    val allFolder : Flow<List<Folder>> = noteDataBase.folderDao().getAllFolder()

    val allRecordRef : Flow<List<RecordRef>> = noteDataBase.recordRefDao().getAllRecordRef()




    /*
    *
    * Note
    *
    * */

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNote(note: Note) : Int {
       return noteDataBase.noteDao().insert(note).toInt()
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


    /*
    *
    * RecordRef
    *
    * */
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertRecordRef(recordRef: RecordRef) {
        Log.d("Insertion", recordRef.recordTitle)
        noteDataBase.recordRefDao().insert(recordRef)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteRecordRef(recordRef: RecordRef) {
        noteDataBase.recordRefDao().delete(recordRef)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateRecordRef(recordRef: RecordRef) {
        noteDataBase.recordRefDao().update(recordRef)
    }

}