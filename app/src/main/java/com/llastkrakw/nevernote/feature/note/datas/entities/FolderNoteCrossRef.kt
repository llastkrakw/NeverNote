package com.llastkrakw.nevernote.feature.note.datas.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["folderId", "noteId"])
data class FolderNoteCrossRef(
        @ColumnInfo(name = "folderId", index = true) var folderId : Int,
        @ColumnInfo(name = "noteId", index = true)var noteId : Int
)
