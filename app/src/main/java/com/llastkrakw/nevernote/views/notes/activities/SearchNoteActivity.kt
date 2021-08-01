package com.llastkrakw.nevernote.views.notes.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.databinding.ActivitySearchNoteBinding
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory

class SearchNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchNoteBinding
    private val noteViewModel : NoteViewModel by viewModels {
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    private lateinit var noteAdapter : NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteAdapter = NoteAdapter(noteViewModel, this)

        binding.apply {

            cancelSearchNote.setOnClickListener {
                onBackPressed()
            }

            noteRecycler.adapter = noteAdapter
            noteRecycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            noteViewModel.allNotesAscWithFolders.observe(this@SearchNoteActivity, { notes ->
                notes?.let { noteAdapter.submitList(it) }
            })

            editTextSearchNote.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    addRightCancelDrawable(editTextSearchNote)
                }

                override fun afterTextChanged(s: Editable?) {
                    noteViewModel.allNotesAscWithFolders.value?.let {
                        noteAdapter.performFiltering(s, it)
                    }
                }

            })

            editTextSearchNote.onRightDrawableClicked {
                it.text.clear()
                it.compoundDrawables[2] = null
            }
        }
    }

    private fun addRightCancelDrawable(editText: EditText) {
        val cancel = ContextCompat.getDrawable(this, R.drawable.ic_cancel)
        cancel?.setBounds(0,0, cancel.intrinsicWidth, cancel.intrinsicHeight)
        editText.setCompoundDrawables(null, null, cancel, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
        this.setOnTouchListener { v, event ->
            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        performClick()
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
    }

}