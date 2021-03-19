package com.llastkrakw.nevernote.core.converters

import android.text.Html
import android.text.SpannableString
import androidx.core.text.toHtml
import androidx.room.TypeConverter

class NoteConverter {

    @TypeConverter
    fun fromSpannable(span : SpannableString) : String{
        return span.toHtml()
    }

    @TypeConverter
    fun toSpannable(value : String) : SpannableString{
        return SpannableString(Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY))
    }

}