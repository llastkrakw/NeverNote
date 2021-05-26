package com.llastkrakw.nevernote.views.notes.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.app.imagepickerlibrary.ImagePickerActivityClass
import com.app.imagepickerlibrary.ImagePickerBottomsheet
import com.app.imagepickerlibrary.bottomSheetActionCamera
import com.app.imagepickerlibrary.bottomSheetActionGallary
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.toSpannable
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.findOptimalOverlayOpacity
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.getPixels
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.getWorstContrastColorInImage
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.resize
import com.llastkrakw.nevernote.databinding.ActivityNoteDetailBinding
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter.Companion.NOTE_EXTRA
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFolders
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import kotlinx.coroutines.*
import java.text.DateFormat


class NoteDetailActivity : AppCompatActivity(), ImagePickerBottomsheet.ItemClickListener, ImagePickerActivityClass.OnResult {

    private val scope = MainScope()
    private lateinit var binding: ActivityNoteDetailBinding
    private var noteWithFolders : NoteWithFolders? = null
    private val noteViewModel : NoteViewModel by viewModels{
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    companion object{
        const val NOTE_UPDATE_EXTRA = "com.llastkrakw.nevernote.notes.update"
        const val UPDATE_NOTE_REQUEST_CODE = 40;
    }


    private val imagePickerFragment = ImagePickerBottomsheet()
    private lateinit var imagePicker : ImagePickerActivityClass


    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker.onActivityResult(requestCode, resultCode, data)
        if(requestCode == UPDATE_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val newNote : NoteWithFolders? = data?.getParcelableExtra(NOTE_UPDATE_EXTRA)
            if(newNote != null){
                Log.d("note_update", newNote.note.noteId.toString())
                setUi(binding, newNote)
                noteViewModel.updateNote(newNote.note)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)

        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        imagePicker = ImagePickerActivityClass(this, this, activityResultRegistry,activity = this)
        imagePicker.cropOptions(true)

        noteWithFolders = intent?.getParcelableExtra(NOTE_EXTRA)

        if (noteWithFolders == null)
            noteWithFolders = intent?.getParcelableExtra(NOTIFICATION_NOTE_EXTRA)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.let {
            title = ""
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        if(noteWithFolders != null)
            setUi(binding, noteWithFolders!!)
    }

    private fun setUi(binding: ActivityNoteDetailBinding, noteWithFolders: NoteWithFolders){

        noteWithFolders.let { note ->

            binding.apply {

                val currentDate = note.note.noteCreatedAt
                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(currentDate)
                noteDateSize.text = String.format("%s | %s characters", dateFormat, note.note.noteContent.length)


                if (note.folders.isEmpty())
                    categorize.text = getText(R.string.uncategorized)
                else
                    categorize.text = String.format("in %s folders", note.folders.size)

                noteTitle.setText(toSpannable(note.note.noteTitle), TextView.BufferType.SPANNABLE)
                noteContent.linksClickable = true
                noteContent.autoLinkMask = Linkify.ALL
                noteContent.movementMethod = LinkMovementMethod.getInstance()
                noteContent.setText(toSpannable(note.note.noteContent), TextView.BufferType.SPANNABLE)

                if(!note.note.noteBg.isNullOrEmpty()){
                    scope.launch {
                        loadBg(noteWithFolders)
                    }
                }

            }

        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){

        R.id.action_delete ->{
            noteWithFolders?.let { noteViewModel.deleteNote(it.note) }
            onBackPressed()
            finish()
            true
        }
        R.id.action_reminder ->{
            noteWithFolders?.let {
                //NoteTimePickerFragment(noteViewModel, it).show(supportFragmentManager, "Select hour of reminder")
                val date = Intent(this, NoteCalendarView::class.java)
                date.putExtra(NOTE_EXTRA, noteWithFolders)
                startActivity(date)
            }
            true
        }
        R.id.action_edit ->{
            noteWithFolders?.let {
                val updateIntent = Intent(this, AddNoteActivity::class.java)
                updateIntent.putExtra(NOTE_UPDATE_EXTRA, it)
                @Suppress("DEPRECATION")
                startActivityForResult(updateIntent, UPDATE_NOTE_REQUEST_CODE)
            }
            true
        }
        R.id.action_share ->{
            shareNote()
            true
        }
        R.id.action_cloth ->{
            imagePickerFragment.show(supportFragmentManager, "image picker")
            true
        }
        else ->{
            super.onOptionsItemSelected(item)
        }

    }

    private fun shareNote(){

        val share = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, noteWithFolders?.note?.let {
                String.format(toSpannable(it.noteTitle).toString() +"\n\n"+ toSpannable(it.noteContent).toString()+"\n\n"+"Shared with NeverNote") }
            )

            // (Optional) Here we're setting the title of the content
            putExtra(Intent.EXTRA_TITLE, "Share note with NeverNote")

            // (Optional) Here we're passing a content URI to an image to be displayed
            data = Uri.parse("android.resource://com.llastkrakw.nevernote/drawable/ic_logo");
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            type= "text/plain"
        }, null)
        startActivity(share)

    }

    private fun loadBg(noteWithFolders: NoteWithFolders){
        Glide
            .with(applicationContext)
            .load(Uri.parse(noteWithFolders.note.noteBg))
            .into(object : CustomTarget<Drawable>(){
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.root.background = resource
                    val pixels = getPixels(resource)
                    val textColor = Color.valueOf(binding.noteContent.currentTextColor)
                    val pixelColor = Color.valueOf(getWorstContrastColorInImage(textColor, pixels))
                    val optimalOpacity = findOptimalOverlayOpacity(textColor, pixelColor, Color.valueOf(Color.WHITE))
                    //binding.root.overlay.add(ColorDrawable(Color.BLACK))
                    binding.main.setBackgroundColor(Color.valueOf(255F, 255F, 255F, optimalOpacity.toFloat()).toArgb())
                    Log.d("opacity optimal", optimalOpacity.toString())
                    Log.d("opacity textColor", textColor.toString())
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

    override fun onItemClick(item: String?) {
        when {
            item.toString() == bottomSheetActionCamera -> {
                imagePicker.takePhotoFromCamera()
            }
            item.toString() == bottomSheetActionGallary -> {
                imagePicker.choosePhotoFromGallery()
            }
        }
    }

    override fun returnString(item: Uri?) {
        noteWithFolders?.let {
            it.note.noteBg = item.toString()
            noteViewModel.updateNote(it.note)
        }
        Glide
            .with(applicationContext)
            .load(item)
            .into(object : CustomTarget<Drawable>(){
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.root.background = resize(resource, binding.root.measuredWidth, binding.root.measuredHeight, this@NoteDetailActivity)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

}