package com.llastkrakw.nevernote.views.notes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.TextView
import androidx.activity.viewModels
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.toSpannable
import com.llastkrakw.nevernote.core.utilities.picassoLoader
import com.llastkrakw.nevernote.databinding.ActivityNoteDetailBinding
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter.Companion.NOTE_EXTRA
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFolders
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import com.llastkrakw.nevernote.views.notes.fragments.NoteTimePickerFragment
import kotlinx.coroutines.*
import java.text.DateFormat

class NoteDetailActivity : AppCompatActivity() {

    private val scope = MainScope()
    private lateinit var binding: ActivityNoteDetailBinding
    private var noteWithFolders : NoteWithFolders? = null
    private val noteViewModel : NoteViewModel by viewModels{
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState)

        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

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
            setUi(binding)
    }

    private fun setUi(binding: ActivityNoteDetailBinding){

        noteWithFolders!!.let {  note ->

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

                if(!note.note.noteBg.isNullOrEmpty())
                    picassoLoader(binding.root, note.note.noteBg!!)

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
                NoteTimePickerFragment(noteViewModel, it).show(supportFragmentManager, "Select hour of reminder")
            }
            true
        }
        else ->{
            super.onOptionsItemSelected(item)
        }

    }
}