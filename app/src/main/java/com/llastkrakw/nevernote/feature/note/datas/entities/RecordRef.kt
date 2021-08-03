package com.llastkrakw.nevernote.feature.note.datas.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.llastkrakw.nevernote.core.constants.RECORD_FOR_THIS_NOTE_ID
import com.llastkrakw.nevernote.core.constants.RECORD_PATH
import com.llastkrakw.nevernote.core.constants.RECORD_TITLE
import com.llastkrakw.nevernote.core.constants.TABLE_RECORD
import kotlinx.parcelize.Parcelize

@Entity(tableName = TABLE_RECORD)
@Parcelize
data class RecordRef(
    @PrimaryKey(autoGenerate = true) val recordId : Long?,
    @ColumnInfo(name = RECORD_PATH) val recordPath : String,
    @ColumnInfo(name = RECORD_FOR_THIS_NOTE_ID) val recordForThisNoteId : Int,
    @ColumnInfo(name = RECORD_TITLE) val recordTitle : String
) : Parcelable
