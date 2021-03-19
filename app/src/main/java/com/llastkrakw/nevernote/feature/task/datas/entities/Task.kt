package com.llastkrakw.nevernote.feature.task.datas.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.llastkrakw.nevernote.core.constants.*
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = TABLE_TASK)
@Parcelize
data class Task(
    @PrimaryKey(autoGenerate = true) var taskId: Long?,
    @ColumnInfo(name = TASK_CONTENT) var taskContent: String,
    @ColumnInfo(name = TASK_CREATED_DATE) var taskCreatedAt: Date?,
    @ColumnInfo(name = TASK_REMINDER) var taskReminder: Date?,
    @ColumnInfo(name = TASK_STATUS) var taskStatus: Boolean
) : Parcelable
