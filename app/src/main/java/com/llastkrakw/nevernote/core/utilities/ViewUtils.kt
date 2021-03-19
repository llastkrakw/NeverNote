package com.llastkrakw.nevernote.core.utilities

import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

class ViewUtils {
    companion object{
        @JvmStatic
        fun getLocationOnScreen(view: View): Point {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return Point(location[0], location[1])
        }

        @JvmStatic
        fun setTextViewDrawableColor(textView: TextView, color: Int) {
            for (drawable in textView.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter =
                            PorterDuffColorFilter(
                                    ContextCompat.getColor(textView.context, color),
                                    PorterDuff.Mode.SRC_IN
                            )
                }
            }
        }
    }
}