package com.llastkrakw.nevernote.feature.note.datas.entities

import android.os.Parcelable
import android.text.SpannableString
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.llastkrakw.nevernote.core.constants.*
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Entity(tableName = TABLE_NOTE)
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true) var noteId: Int?,
    @ColumnInfo(name = NOTE_CONTENT) var noteContent:  String,
    @ColumnInfo(name = NOTE_TITLE) var noteTitle: String,
    @ColumnInfo(name = NOTE_CREATED_DATE) var noteCreatedAt: Date,
    @ColumnInfo(name = NOTE_REMINDER) var noteReminder: Date?,
    @ColumnInfo(name = NOTE_LAST_UPDATE) var noteLastUpdate: Date,
    @ColumnInfo(name = NOTE_BG) var noteBg: String?,
) : Parcelable{

    override fun equals(other: Any?): Boolean {
        return this.noteId === (other as Note).noteId
    }

    override fun hashCode(): Int {
        if (this.noteId != null)
            return super.hashCode() + this.noteId!!
        return super.hashCode()
    }
}