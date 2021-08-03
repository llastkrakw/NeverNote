package com.llastkrakw.nevernote.feature.note.datas.dao

import androidx.room.*
import com.llastkrakw.nevernote.feature.note.datas.entities.RecordRef
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordRefDao {

    @Query("SELECT * FROM TABLE_RECORD")
    fun getAllRecordRef(): Flow<List<RecordRef>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recordRef: RecordRef) : Long

    @Delete
    suspend fun delete(recordRef: RecordRef)

    @Update(onConflict = OnConflictStrategy.REPLACE, entity = RecordRef::class)
    suspend fun update(recordRef: RecordRef)

}