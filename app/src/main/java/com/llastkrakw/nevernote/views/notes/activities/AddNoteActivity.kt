package com.llastkrakw.nevernote.views.notes.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.toHtml
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.llastkrakw.nevernote.BuildConfig
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.utilities.Editor
import com.llastkrakw.nevernote.core.utilities.picassoLoader
import com.llastkrakw.nevernote.databinding.ActivityAddNoteBinding
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.presentation.UnsplashPickerActivity
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*


class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var editor: Editor
    private var isKeyboardActive : Boolean = false
    private var editTextBoxIsVisible : Boolean = false
    private var recreateMenuWatcher : TextWatcher = object : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            invalidateOptionsMenu()
        }

    }

    private var note : Note = Note(null, "", "", Date(), null, Date(), null)

    companion object {
        // dummy request code to identify the request
        private const val IMAGE_REQUEST_CODE = 123
        const val EXTRA_NOTE = "com.llastkrakw.nevernote.new.note"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {
            // getting the photos
            val photos: ArrayList<UnsplashPhoto>? = data?.getParcelableArrayListExtra(
                UnsplashPickerActivity.EXTRA_PHOTOS
            )
            val photo : UnsplashPhoto? = photos?.first()
            Log.d("Photo", photo?.urls.toString())
            // showing the preview
            if (photo != null) {
                note.noteBg = photo.urls.small
                picassoLoader(binding.root, photo.urls.small)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.let {
            title = ""
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        binding.apply {
            editAction.setOnClickListener {
                if (editTextBox.visibility == View.GONE){
                    editTextBoxIsVisible = true
                    editTextBox.visibility = View.VISIBLE
                    editAction.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddNoteActivity,
                            R.drawable.ic_keyboard
                        )
                    )
                    if (isKeyboardActive)
                        hideKeyboard(this@AddNoteActivity)
                }
                else if(editTextBox.visibility == View.VISIBLE){
                    editTextBoxIsVisible = false
                    editTextBox.visibility = View.GONE
                    editAction.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@AddNoteActivity,
                            R.drawable.ic_text_icon
                        )
                    )
                }
            }

            editTextNoteContent.linksClickable = true
            editTextNoteContent.autoLinkMask = Linkify.ALL
            editTextNoteContent.movementMethod = LinkMovementMethod.getInstance()

            editor = Editor(editTextNoteContent)

            editTextNoteContent.addTextChangedListener(recreateMenuWatcher)

            enableStyle()

        }

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff: Int = binding.root.rootView.height - binding.root.height
            isKeyboardActive = heightDiff > dpToPx(this@AddNoteActivity)
            if(isKeyboardActive)
                if(editTextBoxIsVisible)
                    hideKeyboard(this@AddNoteActivity)
        }

    }

    private fun enableStyle(){
        binding.apply {

            makeBold.setOnClickListener {
                editor.makeBold()
            }

            makeItalic.setOnClickListener {
                editor.makeItalic()
            }

            makeUnderline.setOnClickListener {
                editor.makeUnderline()
            }

            increaseSize.setOnClickListener {
                editor.increaseSize()
            }

            alignLeft.setOnClickListener {
                editor.alignLeft()
            }

            alignRight.setOnClickListener {
                editor.alignRight()
            }

            addColor.setOnClickListener {
                ColorPickerDialog
                        .Builder(this@AddNoteActivity)
                        .setColorShape(ColorShape.CIRCLE)
                        .setColorListener { color, _ ->
                            editor.setColor(color)
                        }
                        .show()
            }

            addLink.setOnClickListener {
                editor.setUrl(
                    editTextNoteContent.text.subSequence(
                        editTextNoteContent.selectionStart,
                        editTextNoteContent.selectionEnd
                    )
                )
            }

        }

        binding.addCloth.setOnClickListener {
            @Suppress("DEPRECATION")
            startActivityForResult(
                UnsplashPickerActivity.getStartingIntent(
                    this,
                    false
                ), IMAGE_REQUEST_CODE
            )
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.add_note_mennu, menu)
        val undo = menu?.findItem(R.id.undo_action)
        val redo = menu?.findItem(R.id.redo_action)
        val check = menu?.findItem(R.id.check_action)

        if (editor.undoIsEnable()){
            undo?.icon = ContextCompat.getDrawable(this, R.drawable.ic_undo)
        }
        else{
            undo?.icon = ContextCompat.getDrawable(this, R.drawable.ic_undo_disable)
        }
        if (editor.redoIsEnable()){
            redo?.icon = ContextCompat.getDrawable(this, R.drawable.ic_redo)
        }
        else{
            redo?.icon = ContextCompat.getDrawable(this, R.drawable.ic_redo_disable)
        }
        if(binding.editTextNoteContent.text.isNotEmpty()){
            check?.icon = ContextCompat.getDrawable(this, R.drawable.ic_checkmark)
            check?.isEnabled = true
        }
        else{
            check?.icon = ContextCompat.getDrawable(this, R.drawable.ic_checkmark_disable)
            check?.isEnabled = false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.undo_action -> {
            editor.undo()
            invalidateOptionsMenu()
            true
        }
        R.id.redo_action -> {
            editor.redo()
            invalidateOptionsMenu()
            true
        }
        R.id.check_action -> {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(binding.editTextNoteContent.text) && TextUtils.isEmpty(binding.editTextNoteTitle.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }
            else{
                note.noteTitle = binding.editTextNoteTitle.text.toHtml()
                note.noteContent = binding.editTextNoteContent.text.toHtml()
                note.noteCreatedAt = Date()
                note.noteLastUpdate = Date()
                replyIntent.putExtra(EXTRA_NOTE, note)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            onBackPressed()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun dpToPx(context: Context): Float {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200F, metrics)
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}