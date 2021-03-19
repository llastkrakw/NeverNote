package com.llastkrakw.nevernote.core.utilities

import java.text.DateFormat
import java.util.*

class FormatUtils {

    companion object{
        @JvmStatic
        fun toSimpleString(date: Date) : String {
            val format = DateFormat.getDateInstance().format(date)
            return format.format(date)
        }
    }

}