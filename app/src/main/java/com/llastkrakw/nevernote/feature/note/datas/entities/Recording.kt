package com.llastkrakw.nevernote.feature.note.datas.entities

data class Recording(
    val id: Int,
    val title: String,
    val path: String,
    val timestamp: Int,
    val duration: Int,
    val size: Int,
)
