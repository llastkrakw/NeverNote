package com.llastkrakw.nevernote.feature.note.datas.dao

import androidx.room.*
import com.llastkrakw.nevernote.core.constants.NOTE_LAST_UPDATE
import com.llastkrakw.nevernote.core.constants.NOTE_TITLE
import com.llastkrakw.nevernote.core.constants.TABLE_NOTE
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFoldersAndRecords
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM TABLE_NOTE ORDER BY $NOTE_TITLE ASC")
    fun getAlphabetizedNotes(): Flow<List<Note>>

    @Transaction
    @Query("SELECT * FROM TABLE_NOTE ORDER BY $NOTE_TITLE ASC")
    fun getAlphabetizedNotesWithFoldersAndRecords(): Flow<List<NoteWithFoldersAndRecords>>

    @Query("SELECT * FROM TABLE_NOTE ORDER BY $NOTE_LAST_UPDATE ASC")
    fun getLastUpdateNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note) : Long

    @Delete
    suspend fun delete(notes: List<Note>)

    @Delete
    suspend fun delete(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE, entity = Note::class)
    suspend fun update(note: Note)

    @Update(onConflict = OnConflictStrategy.REPLACE, entity = Note::class)
    suspend fun update(notes: List<Note>)

    @Query("DELETE FROM $TABLE_NOTE")
    suspend fun deleteAll()
}
