package com.llastkrakw.nevernote.views.notes.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.toHtml
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
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.extension.*
import com.llastkrakw.nevernote.core.utilities.Editor
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.toSpannable
import com.llastkrakw.nevernote.core.utilities.SwipeCallback
import com.llastkrakw.nevernote.core.utilities.ViewUtils
import com.llastkrakw.nevernote.databinding.ActivityAddNoteBinding
import com.llastkrakw.nevernote.feature.note.adapters.RecordAdapter
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFoldersAndRecords
import com.llastkrakw.nevernote.feature.note.datas.entities.RecordRef
import com.llastkrakw.nevernote.feature.note.datas.entities.Recording
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import com.llastkrakw.nevernote.views.notes.activities.NoteDetailActivity.Companion.NOTE_UPDATE_EXTRA
import com.llastkrakw.nevernote.views.notes.fragments.RecordDialogFragment
import com.llastkrakw.nevernote.views.notes.fragments.RecordDialogFragment.Companion.NOTE_ID_FOR_SERVICE
import java.util.*


class AddNoteActivity : AppCompatActivity(), ImagePickerBottomsheet.ItemClickListener, ImagePickerActivityClass.OnResult {

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private val noteViewModel : NoteViewModel by viewModels{
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

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


    private var note : Note = Note(null, "", "", Date(), null, Date(), null, null)
    private var noteId : Int? = null
    private var noteForUpdate : NoteWithFoldersAndRecords? = null

    private val imagePickerFragment = ImagePickerBottomsheet()
    private lateinit var imagePicker : ImagePickerActivityClass

    private var recordAdapter : RecordAdapter = RecordAdapter()


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return
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

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent, binding) // Handle text being sent
                }
            }
        }

        imagePicker = ImagePickerActivityClass(this, this, activityResultRegistry,activity = this)
        imagePicker.cropOptions(true)

        noteForUpdate = intent?.getParcelableExtra(NOTE_UPDATE_EXTRA)

        if(noteForUpdate == null){
            noteViewModel.insertNote(note)
            noteViewModel.returnedIdWhenAdd.observe(this, {
                noteId = it
                note.noteId = it
            })
        }
        else
            noteId = noteForUpdate!!.note.noteId


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
                            R.drawable.ic_sentence_case
                        )
                    )
                }
            }

            editTextNoteContent.linksClickable = true
            editTextNoteContent.autoLinkMask = Linkify.ALL
            editTextNoteContent.movementMethod = LinkMovementMethod.getInstance()

            editor = Editor(editTextNoteContent)

            editTextNoteContent.addTextChangedListener(recreateMenuWatcher)

            if(noteForUpdate != null){
                editTextNoteContent.setText(toSpannable(noteForUpdate!!.note.noteContent), TextView.BufferType.SPANNABLE)
                editTextNoteTitle.setText(toSpannable(noteForUpdate!!.note.noteTitle), TextView.BufferType.SPANNABLE)
            }

            enableStyle()

            recordRecycler.layoutManager = LinearLayoutManager(this@AddNoteActivity)

            noteViewModel.allRecordRef.observe(this@AddNoteActivity, {
                recordAdapter.submitList(getRecordForNote(noteForUpdate, noteId!!, it))
            })

            recordRecycler.adapter = recordAdapter

            val swipeCompleteCallback = object : SwipeCallback(this@AddNoteActivity){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    noteViewModel.allRecordRef.observe(this@AddNoteActivity, {
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

            addBullet.setOnClickListener {
                if (editTextNoteContent.selectionStart != editTextNoteContent.selectionEnd)
                    editor.makeBulletList(editTextNoteContent.text.subSequence(
                        editTextNoteContent.selectionStart,
                        editTextNoteContent.selectionEnd
                    ).split(" ").map { it.trim() })
            }

            addLink.setOnClickListener {
                if (editTextNoteContent.selectionStart != editTextNoteContent.selectionEnd)
                    editor.setUrl(
                        editTextNoteContent.text.subSequence(
                            editTextNoteContent.selectionStart,
                            editTextNoteContent.selectionEnd
                        )
                    )
            }

            addRecorder.setOnClickListener {
                if (!permissionsIsGranted(requiredPermissions)) {
                    ActivityCompat.requestPermissions(this@AddNoteActivity, requiredPermissions, 200)
                }
                showRecordDialog()
            }

        }

       binding.addCloth.setOnClickListener {
/*            @Suppress("DEPRECATION")
            startActivityForResult(
                UnsplashPickerActivity.getStartingIntent(
                    this,
                    false
                ), IMAGE_REQUEST_CODE
            )*/

           imagePickerFragment.show(supportFragmentManager, "image picker")
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
            if (noteForUpdate != null){
                Log.d("note_update", "in add ${noteForUpdate!!.note.noteId}")
                noteForUpdate!!.note.noteTitle = binding.editTextNoteTitle.text.toHtml()
                noteForUpdate!!.note.noteContent = binding.editTextNoteContent.text.toHtml()
                replyIntent.putExtra(NOTE_UPDATE_EXTRA, noteForUpdate)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            else{
                if (!(TextUtils.isEmpty(binding.editTextNoteContent.text) && TextUtils.isEmpty(binding.editTextNoteTitle.text))) {
                    note.noteTitle = binding.editTextNoteTitle.text.toHtml()
                    note.noteContent = binding.editTextNoteContent.text.toHtml()
                    noteViewModel.updateNote(note)
                    Log.d("note_body", "noteBody ${binding.editTextNoteContent.text.toHtml()}")
                }
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
        noteViewModel.deleteNote(note)
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
        if(noteForUpdate == null){
            note.noteBg = item.toString()
        }
        else{
            noteForUpdate!!.note.noteBg = item.toString()
        }
        Glide
            .with(applicationContext)
            .load(item)
            .into(object : CustomTarget<Drawable>(){
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    binding.root.background = ViewUtils.resize(
                        resource,
                        binding.root.measuredWidth,
                        binding.root.measuredHeight,
                        this@AddNoteActivity
                    )
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
    }

    private fun showRecordDialog(){

        val dialogFragment = RecordDialogFragment()
        val bundle = Bundle()

        noteId?.let { bundle.putInt(NOTE_ID_FOR_SERVICE, it) }

        dialogFragment.arguments = bundle
        dialogFragment.show(supportFragmentManager, RecordDialogFragment.TAG)
    }

    private fun permissionsIsGranted(perms: Array<String>): Boolean {
        for (perm in perms) {
            val checkVal: Int = checkCallingOrSelfPermission(perm)
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun getRecordForNote(noteForUpdate : NoteWithFoldersAndRecords?, noteId : Int, recordsRef: List<RecordRef>) : List<Recording> {

        val recordings = noteViewModel.getRecordings().toList()
        val selectedRecords = mutableListOf<Recording>()

        Log.d("recording size", recordings.size.toString())

        if (noteForUpdate != null)
            Log.d("record ref size", noteForUpdate.recordsRef.size.toString())

        recordings.forEach {
            if (noteForUpdate == null){
                recordsRef.forEach { recordRef ->
                    if(noteId == recordRef.recordForThisNoteId && it.title == recordRef.recordTitle){
                        Log.d("record size", noteId.toString())
                        Log.d("record size", recordRef.recordForThisNoteId.toString())
                        selectedRecords.add(it)
                        Log.d("record size", it.title)
                    }
                }
            }
            else{
                recordsRef.forEach{ recordRef ->
                    Log.d("record size", noteId.toString())
                    Log.d("record size", recordRef.recordForThisNoteId.toString())
                    if(noteId == recordRef.recordForThisNoteId && it.title == recordRef.recordTitle){
                        selectedRecords.add(it)
                        Log.d("record size", it.title)
                    }

                }
            }
        }


        Log.d("selected record size", selectedRecords.size.toString())
        return selectedRecords.toList()
    }

    private fun handleSendText(intent: Intent, binding: ActivityAddNoteBinding) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
           binding.editTextNoteContent.setText(it, TextView.BufferType.EDITABLE)
        }
    }
}