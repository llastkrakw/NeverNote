package com.llastkrakw.nevernote.feature.note.datas.dao

import androidx.room.*
import com.llastkrakw.nevernote.core.constants.TABLE_FOLDER
import com.llastkrakw.nevernote.feature.note.datas.entities.Folder
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderWithNotes
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Transaction
    @Query("SELECT * FROM $TABLE_FOLDER")
    fun getAllFolderWithNote() : Flow<List<FolderWithNotes>>

    @Query("SELECT * FROM $TABLE_FOLDER")
    fun getAllFolder() : Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folder: Folder) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(folder: Folder)

    @Delete
    suspend fun delete(folder: Folder)

    @Query("DELETE FROM $TABLE_FOLDER")
    suspend fun deleteAll()

}