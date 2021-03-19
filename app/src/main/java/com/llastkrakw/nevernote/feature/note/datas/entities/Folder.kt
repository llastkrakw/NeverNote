package com.llastkrakw.nevernote.feature.note.datas.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.llastkrakw.nevernote.core.constants.FOLDER_CREATED_DATE
import com.llastkrakw.nevernote.core.constants.FOLDER_NAME
import com.llastkrakw.nevernote.core.constants.TABLE_FOLDER
import kotlinx.parcelize.Parcelize
import java.util.*


@Entity(tableName = TABLE_FOLDER)
@Parcelize
data class Folder(
    @PrimaryKey(autoGenerate = true) val folderId : Int?,
    @ColumnInfo(name = FOLDER_NAME) val folderName : String,
    @ColumnInfo(name = FOLDER_CREATED_DATE) val folderCreatedAt : Date
) : Parcelable
