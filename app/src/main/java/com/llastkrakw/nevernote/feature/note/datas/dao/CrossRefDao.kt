package com.llastkrakw.nevernote.feature.note.datas.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderNoteCrossRef

@Dao
interface CrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folderNoteCrossRef: FolderNoteCrossRef) : Long
}