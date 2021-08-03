package com.llastkrakw.nevernote.feature.note.datas.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.llastkrakw.nevernote.core.constants.NOTE_FOLDER_ID
import com.llastkrakw.nevernote.core.constants.RECORD_FOR_THIS_NOTE_ID
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteWithFoldersAndRecords(
    @Embedded val note : Note,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "folderId",
        associateBy = Junction(FolderNoteCrossRef::class)
    )
    val folders : List<Folder>,
    @Relation(
        parentColumn = "noteId",
        entityColumn = RECORD_FOR_THIS_NOTE_ID,
        entity = RecordRef::class
    )
    val recordsRef : List<RecordRef>
) : Parcelable
