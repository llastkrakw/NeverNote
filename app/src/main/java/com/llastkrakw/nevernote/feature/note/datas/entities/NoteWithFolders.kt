package com.llastkrakw.nevernote.feature.note.datas.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.llastkrakw.nevernote.core.constants.NOTE_FOLDER_ID
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteWithFolders(
    @Embedded val note : Note,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "folderId",
        associateBy = Junction(FolderNoteCrossRef::class)
    )
    val folders : List<Folder>
) : Parcelable
