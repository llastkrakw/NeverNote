package com.llastkrakw.nevernote.views.notes.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.transition.Fade
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.imagepickerlibrary.ImagePickerActivityClass
import com.app.imagepickerlibrary.ImagePickerBottomsheet
import com.app.imagepickerlibrary.bottomSheetActionCamera
import com.app.imagepickerlibrary.bottomSheetActionGallary
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.BACK_SONG
import com.llastkrakw.nevernote.core.constants.DELETE_SONG
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.constants.TAP_SONG
import com.llastkrakw.nevernote.core.extension.dateExpired
import com.llastkrakw.nevernote.core.extension.playUiSong
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.toSpannable
import com.llastkrakw.nevernote.core.utilities.SwipeCallback
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.findOptimalOverlayOpacity
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.getPixels
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.getWorstContrastColorInImage
import com.llastkrakw.nevernote.core.utilities.ViewUtils.Companion.resize
import com.llastkrakw.nevernote.databinding.ActivityNoteDetailBinding
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter.Companion.NOTE_EXTRA
import com.llastkrakw.nevernote.feature.note.adapters.RecordAdapter
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFoldersAndRecords
import com.llastkrakw.nevernote.feature.note.datas.entities.RecordRef
import com.llastkrakw.nevernote.feature.note.datas.entities.Recording
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import kotlinx.coroutines.*
import java.text.DateFormat


class NoteDetailActivity : AppCompatActivity(), ImagePickerBottomsheet.ItemClickListener, ImagePickerActivityClass.OnResult {

    private val scope = MainScope()
    private lateinit var binding: ActivityNoteDetailBinding
    private var noteWithFolders : NoteWithFoldersAndRecords? = null
    private val noteViewModel : NoteViewModel by viewModels{
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    companion object{
        const val NOTE_UPDATE_EXTRA = "com.llastkrakw.nevernote.notes.update"
        const val UPDATE_NOTE_REQUEST_CODE = 40
    }

    private lateinit var isDark : Any

    private val imagePickerFragment = ImagePickerBottomsheet()
    private lateinit var imagePicker : ImagePickerActivityClass

    private var recordAdapter : RecordAdapter = RecordAdapter()

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker.onActivityResult(requestCode, resultCode, data)
        if(requestCode == UPDATE_NOTE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val newNote : NoteWithFoldersAndRecords? = data?.getParcelableExtra(NOTE_UPDATE_EXTRA)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onCreate(savedInstanceState)

        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpWindowAnimations()
        binding.lifecycleOwner = this

        isDark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUi(binding: ActivityNoteDetailBinding, noteWithFolders: NoteWithFoldersAndRecords){

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
                    Log.d("bg_issue", "bag is ${note.note.noteBg}")
                    scope.launch {
                        loadBg(noteWithFolders)
                    }
                }
                else
                    Log.d("bg_issue", "bag is ${note.note.noteBg}")

                haveClock.visibility = if(note.note.noteReminder != null && !note.note.noteReminder!!.dateExpired()) View.VISIBLE else View.GONE


                recordRecycler.layoutManager = LinearLayoutManager(this@NoteDetailActivity)

                noteViewModel.allRecordRef.observe(this@NoteDetailActivity, {
                    recordAdapter.submitList(getRecordForNote(it))
                })

                recordRecycler.adapter = recordAdapter

                val swipeCompleteCallback = object : SwipeCallback(this@NoteDetailActivity){
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        playUiSong(DELETE_SONG)
                        noteViewModel.allRecordRef.observe(this@NoteDetailActivity, {
                            val position = viewHolder.absoluteAdapterPosition
                            val recording = recordAdapter.currentList[position]
                            it.forEach { recordRef ->
                                if(recordRef.recordTitle == recording.title){
                                    noteViewModel.deleteRecordRef(recordRef)
                                }
                            }
                        })
                    }
                }

                val itemTouch = ItemTouchHelper(swipeCompleteCallback)
                itemTouch.attachToRecyclerView(recordRecycler)

            }

        }
    }

    private fun getRecordForNote(recordRefs: List<RecordRef>?): MutableList<Recording> {
        val recordings = noteViewModel.getRecordings().toList()
        val selectedRecords = mutableListOf<Recording>()

        noteWithFolders?.note?.noteId.let { noteId ->

            recordings.forEach {
                recordRefs?.forEach{ recordRef ->
                    Log.d("record size", noteId.toString())
                    Log.d("record size", recordRef.recordForThisNoteId.toString())
                    if(noteId == recordRef.recordForThisNoteId && it.title == recordRef.recordTitle){
                        selectedRecords.add(it)
                        Log.d("record size", it.title)
                    }

                }
            }

        }
        return selectedRecords
    }


    override fun onSupportNavigateUp(): Boolean {
        this.playUiSong(BACK_SONG)
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
            this.playUiSong(DELETE_SONG)
            noteWithFolders?.let { noteViewModel.deleteNote(it.note) }
            onBackPressed()
            finish()
            true
        }
        R.id.action_reminder ->{
            this.playUiSong(TAP_SONG)
            noteWithFolders?.let {
                //NoteTimePickerFragment(noteViewModel, it).show(supportFragmentManager, "Select hour of reminder")
                val date = Intent(this, NoteCalendarView::class.java)
                date.putExtra(NOTE_EXTRA, noteWithFolders)
                startActivity(date)
            }
            true
        }
        R.id.action_edit ->{
            this.playUiSong(TAP_SONG)
            noteWithFolders?.let {
                val updateIntent = Intent(this, AddNoteActivity::class.java)
                updateIntent.putExtra(NOTE_UPDATE_EXTRA, it)
                @Suppress("DEPRECATION")
                startActivityForResult(updateIntent, UPDATE_NOTE_REQUEST_CODE)
            }
            true
        }
        R.id.action_share ->{
            this.playUiSong(TAP_SONG)
            shareNote()
            true
        }
        R.id.action_cloth ->{
            this.playUiSong(TAP_SONG)
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
                String.format(toSpannable(it.noteTitle).toString() +"\n\n"+ toSpannable(it.noteContent).toString()+"\n\n"+"Shared with NeverNote," +
                        " you can download on playstore https://bit.ly/3khjZt0") }
            )

            // (Optional) Here we're setting the title of the content
            putExtra(Intent.EXTRA_TITLE, "Share note with NeverNote")

            // (Optional) Here we're passing a content URI to an image to be displayed
            data = Uri.parse("android.resource://com.llastkrakw.nevernote/drawable/ic_logo")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            type= "text/plain"
        }, null)
        startActivity(share)

    }

    private fun loadBg(noteWithFolders: NoteWithFoldersAndRecords){
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
                    correctContrast(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun correctContrast(resource : Drawable){
        val pixels = getPixels(resource)
        val textColor = Color.valueOf(binding.noteContent.currentTextColor)
        val pixelColor = Color.valueOf(getWorstContrastColorInImage(textColor, pixels))
        //binding.root.overlay.add(ColorDrawable(Color.BLACK))v
        when (isDark) {
            Configuration.UI_MODE_NIGHT_NO -> {
                val optimalOpacity = findOptimalOverlayOpacity(textColor, pixelColor, Color.valueOf(Color.WHITE))
                binding.main.setBackgroundColor(Color.valueOf(255F, 255F, 255F, optimalOpacity.toFloat()).toArgb())
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                val optimalOpacity = findOptimalOverlayOpacity(textColor, pixelColor, Color.valueOf(Color.BLACK))
                binding.main.setBackgroundColor(Color.valueOf(0F, 0F, 0F, optimalOpacity.toFloat()).toArgb())
            } // Night mode is active, we're using dark theme
        }
        Log.d("opacity textColor", textColor.toString())
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
        noteWithFolders!!.note.let {
            it.noteBg = item.toString()
            Log.d("bg_issue", "bag in return String is ${it.noteBg}")
            noteViewModel.updateNote(it)
        }
        Glide
            .with(applicationContext)
            .load(item)
            .into(object : CustomTarget<Drawable>(){
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.root.background = resize(resource, binding.root.measuredWidth, binding.root.measuredHeight, this@NoteDetailActivity)
                    correctContrast(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                isDark = Configuration.UI_MODE_NIGHT_NO
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                isDark = Configuration.UI_MODE_NIGHT_YES
            } // Night mode is active, we're using dark theme
        }
    }

    private fun setUpWindowAnimations(){
        val slide = Fade().setDuration(1000)
        window.enterTransition = slide
    }
}