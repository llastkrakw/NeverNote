package com.llastkrakw.nevernote.core.utilities

import android.graphics.Color
import android.graphics.Typeface
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.core.text.toHtml
import androidx.core.text.toSpannable


class SpanUtils {

    companion object{

        @JvmStatic
        fun spanToHtml(value: SpannableString) : String{
            return value.toHtml()
        }

        @JvmStatic
        fun toSpannable(value: String) : SpannableString{
            return SpannableString(Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT))
        }
    }

}

fun spannable(func: () -> SpannableString) = func()
private fun span(s: CharSequence, o: Any, editText: EditText) =
    (if (s is String) SpannableString(s) else s as? SpannableString
        ?: SpannableString("")).apply {
            setSpan(o, editText.selectionStart, editText.selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            editText.setText(this, TextView.BufferType.SPANNABLE)
        }

operator fun SpannableString.plus(s: SpannableString) = SpannableString(TextUtils.concat(this, s))
operator fun SpannableString.plus(s: String) = SpannableString(TextUtils.concat(this, s))

fun bold(s: CharSequence, editText: EditText) = span(s, StyleSpan(Typeface.BOLD), editText)
fun bold(s: SpannableString, editText: EditText) = span(s, StyleSpan(Typeface.BOLD), editText)
fun italic(s: CharSequence, editText: EditText) = span(s, StyleSpan(Typeface.ITALIC), editText)
fun italic(s: SpannableString, editText: EditText) = span(s, StyleSpan(Typeface.ITALIC), editText)
fun underline(s: CharSequence, editText: EditText) = span(s, UnderlineSpan(), editText)
fun underline(s: SpannableString, editText: EditText) = span(s, UnderlineSpan(), editText)
fun strike(s: CharSequence, editText: EditText) = span(s, StrikethroughSpan(), editText)
fun strike(s: SpannableString, editText: EditText) = span(s, StrikethroughSpan(), editText)
fun sup(s: CharSequence, editText: EditText) = span(s, SuperscriptSpan(), editText)
fun sup(s: SpannableString, editText: EditText) = span(s, SuperscriptSpan(), editText)
fun sub(s: CharSequence, editText: EditText) = span(s, SubscriptSpan(), editText)
fun sub(s: SpannableString, editText: EditText) = span(s, SubscriptSpan(), editText)
fun size(size: Float, s: CharSequence, editText: EditText) = span(s, RelativeSizeSpan(size), editText)
fun size(size: Float, s: SpannableString, editText: EditText) = span(s, RelativeSizeSpan(size), editText)
fun setColor(color: Int, s: CharSequence, editText: EditText) = span(s, ForegroundColorSpan(color), editText)
fun setColor(color: Int, s: SpannableString, editText: EditText) = span(s, ForegroundColorSpan(color), editText)
fun background(color: Int, s: CharSequence, editText: EditText) = span(s, BackgroundColorSpan(color), editText)
fun background(color: Int, s: SpannableString, editText: EditText) = span(s, BackgroundColorSpan(color), editText)
fun url(url: String, s: CharSequence, editText: EditText) = span(s, URLSpan(url), editText)
fun url(url: String, s: SpannableString, editText: EditText) = span(s, URLSpan(url), editText)
fun normal(s: CharSequence, editText: EditText) = span(s, SpannableString(s), editText)
fun normal(s: SpannableString, editText: EditText) = span(s, SpannableString(s), editText)

fun convertToBulletList(stringList: List<String>): CharSequence {
    val spannableStringBuilder = SpannableStringBuilder()
    stringList.forEachIndexed { index, text ->
        val line: CharSequence = text + if (index < stringList.size - 1) "\n" else ""
        val spannable: Spannable = SpannableString(line)
        spannable.setSpan(
            BulletSpan(15, Color.GREEN),
            0,
            spannable.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.append(spannable)
    }
    return spannableStringBuilder
}

fun spanUnderline(editText: EditText) {
    val spannableString: Spannable = SpannableStringBuilder(editText.text)
    spannableString.setSpan(
        UnderlineSpan(),
        editText.selectionStart,
        editText.selectionEnd,
        0
    )
    editText.setText(spannableString)
}

fun spanAlignmentLeft(editText: EditText) {
    editText.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
    val spannableString: Spannable = SpannableStringBuilder(editText.text)
    editText.setText(spannableString)
}

fun spanAlignmentCenter(editText: EditText) {
    editText.textAlignment = View.TEXT_ALIGNMENT_CENTER
    val spannableString: Spannable = SpannableStringBuilder(editText.text)
    editText.setText(spannableString)
}

fun spanAlignmentRight(editText: EditText) {
    editText.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
    val spannableString: Spannable = SpannableStringBuilder(editText.text)
    editText.setText(spannableString)
}