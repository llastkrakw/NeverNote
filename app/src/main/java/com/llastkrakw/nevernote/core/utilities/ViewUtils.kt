package com.llastkrakw.nevernote.core.utilities

import android.content.Context
import android.content.DialogInterface
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.feature.note.datas.entities.Folder
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderWithNotes
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import java.util.*

class ViewUtils {

    companion object{

        private const val DESIRED_CONTRAST = 4.5

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

        @JvmStatic
        fun showAddFolderDialog(noteViewModel: NoteViewModel, context: Context, layoutInflater : LayoutInflater){
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            val folderView = layoutInflater.inflate(R.layout.add_folder, null)

            builder.setView(folderView)
            val alertDialog = builder.create()
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val editText = folderView.findViewById<EditText>(R.id.add_folder_edit_text)
            val addButton = folderView.findViewById<TextView>(R.id.button_add_folder)
            val cancelButton = folderView.findViewById<TextView>(R.id.add_folder_cancel)

            addButton.setOnClickListener(){
                editText.text?.let {
                    if(it.toString().isNotEmpty()){
                        val folder = Folder(null, it.toString(), Date())
                        noteViewModel.insertFolder(folder)
                        alertDialog.cancel()
                    }
                }
            }

            cancelButton.setOnClickListener(){
                alertDialog.cancel()
            }

            alertDialog.show()
        }

        @JvmStatic
        fun resize(image: Drawable, width: Int, height : Int, context: Context): Drawable {
            val b = (image as BitmapDrawable).bitmap
            val bitmapResized = Bitmap.createScaledBitmap(b, width, height, false)
            return BitmapDrawable(context.resources, bitmapResized)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getPixels(drawable: Drawable) : IntArray{
            val bitmap = (drawable as BitmapDrawable).bitmap
            val pixels = IntArray(bitmap.height * bitmap.width)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            Log.d("pixels", String.format("#%06X", (pixels[1])))

            return pixels
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getContrast(firstColor : Color, secondColor : Color) : Double{
            val firstLuminance = firstColor.luminance()
            val  secondLuminance = secondColor.luminance()
            val lighterColorLuminance = firstLuminance.coerceAtLeast(secondLuminance)
            val darkColorLuminance = firstLuminance.coerceAtMost(secondLuminance)

            return (lighterColorLuminance + 0.05) / (darkColorLuminance + 0.05)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getWorstContrastColorInImage(textColor : Color, pixelColors : IntArray) : Int{
            var worstContrastColorInImage : Int = pixelColors[0]
            var worstContrast : Double = getContrast(textColor, Color.valueOf(pixelColors[0]))

            pixelColors.forEach { pixelColor ->

                val contrast = getContrast(textColor, Color.valueOf(pixelColor))
                if(contrast < worstContrast){
                    worstContrast = contrast
                    worstContrastColorInImage = pixelColor
                }
            }

            return worstContrastColorInImage
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun mixedColor(pixelColor : Color, overlayColor : Color, opacity : Float) : Color{
            return  Color.valueOf(
                pixelColor.red() + (overlayColor.red() - pixelColor.red()) * opacity,
                pixelColor.green() + (overlayColor.green() - pixelColor.green()) * opacity,
                pixelColor.blue() + (overlayColor.blue() - pixelColor.blue()) * opacity
            )
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getContrastWithOverlay(textColor : Color, pixelColor : Color, opacity : Float, overlayColor : Color) : Double{
            val mixedColor = mixedColor(pixelColor, overlayColor, opacity)
            return getContrast(textColor, mixedColor)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun isOverlayNecessary(textColor: Color, pixelColor: Color, desiredContrast : Double = DESIRED_CONTRAST) : Boolean{
            return getContrast(textColor, pixelColor) < desiredContrast
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun findOptimalOverlayOpacity(textColor: Color, pixelColor : Color, overlayColor : Color) : Double{

            var lowerBound = 0.0
            var midPoint = 0.5
            var upperBound = 1.0

            val maxGuesses = 4
            val opacityLimit = 0.99

            if (!isOverlayNecessary(textColor, pixelColor))
                return 0.0


            for (i in 1..maxGuesses){

                val currentGuess = midPoint
                val contrastGuess = getContrastWithOverlay(textColor, pixelColor, currentGuess.toFloat(), overlayColor)

                Log.d("opacity contrast", currentGuess.toString())

                val isGuessToLow = contrastGuess < DESIRED_CONTRAST

                if(isGuessToLow)
                    lowerBound = currentGuess
                else
                    upperBound = currentGuess

                midPoint = (upperBound + lowerBound) / 2
            }

            val optimalOpacity = midPoint
            Log.d("opacity contrast", optimalOpacity.toString())
            val hasNotSolution = optimalOpacity > opacityLimit

            if (hasNotSolution)
                return opacityLimit

            return optimalOpacity
        }
    }
}