package com.llastkrakw.nevernote.core.extension

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat.getDrawable

fun getColoredBitmap(resourceId: Int, newColor: Int, context: Context): Bitmap {
    val drawable = getDrawable(context.resources, resourceId, Resources.getSystem().newTheme())!!
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.colorFilter = PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_IN)
    drawable.draw(canvas)
    return bitmap
}

fun Resources.getColoredDrawable(drawableId: Int, colorId: Int, alpha: Int = 255, context: Context) =
    getColoredDrawableWithColor(drawableId, getColor(colorId, Resources.getSystem().newTheme()), alpha, context)

fun getColoredDrawableWithColor(drawableId: Int, color: Int, alpha: Int = 255, context: Context): Drawable {
    val drawable = getDrawable(context.resources, drawableId, Resources.getSystem().newTheme())!!
    drawable.mutate().applyColorFilter(color)
    drawable.mutate().alpha = alpha
    return drawable
}
