package com.llastkrakw.nevernote.core.config

import android.content.Context

import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.*
import com.llastkrakw.nevernote.core.extension.getInternalStoragePath
import com.llastkrakw.nevernote.core.extension.getSDCardPath
import com.llastkrakw.nevernote.core.extension.getSharedPrefs


class Config(val context: Context){

    private val prefs = context.getSharedPrefs()


    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    private fun getDefaultInternalPath() = if (prefs.contains(INTERNAL_STORAGE_PATH)) "" else getInternalStoragePath()
    private fun getDefaultSDCardPath() = if (prefs.contains(SD_CARD_PATH)) "" else context.getSDCardPath()

    var sdCardPath: String
        get() = prefs.getString(SD_CARD_PATH, getDefaultSDCardPath())!!
        set(sdCardPath) = prefs.edit().putString(SD_CARD_PATH, sdCardPath).apply()

    var internalStoragePath: String
        get() = prefs.getString(INTERNAL_STORAGE_PATH, getDefaultInternalPath())!!
        set(internalStoragePath) = prefs.edit().putString(INTERNAL_STORAGE_PATH, internalStoragePath).apply()

    var saveRecordingsFolder: String
        get() = prefs.getString(SAVE_RECORDINGS, "$internalStoragePath/${context.getString(R.string.app_name)}")!!
        set(saveRecordingsFolder) = prefs.edit().putString(SAVE_RECORDINGS, saveRecordingsFolder).apply()

    var extension: Int
        get() = prefs.getInt(EXTENSION, EXTENSION_M4A)
        set(extension) = prefs.edit().putInt(EXTENSION, extension).apply()

    var OTGPartition: String
        get() = prefs.getString(OTG_PARTITION, "")!!
        set(OTGPartition) = prefs.edit().putString(OTG_PARTITION, OTGPartition).apply()

    var OTGPath: String
        get() = prefs.getString(OTG_REAL_PATH, "")!!
        set(OTGPath) = prefs.edit().putString(OTG_REAL_PATH, OTGPath).apply()

    var OTGTreeUri: String
        get() = prefs.getString(OTG_TREE_URI, "")!!
        set(OTGTreeUri) = prefs.edit().putString(OTG_TREE_URI, OTGTreeUri).apply()

    var treeUri: String
        get() = prefs.getString(TREE_URI, "")!!
        set(uri) = prefs.edit().putString(TREE_URI, uri).apply()


    fun getExtensionText() = context.getString(when (extension) {
        EXTENSION_M4A -> R.string.m4a
        else -> R.string.mp3
    })


}
