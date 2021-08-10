package com.llastkrakw.nevernote.views.notes.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.databinding.ActivitySearchNoteBinding
import com.llastkrakw.nevernote.feature.note.adapters.AddFolderAdapter
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter
import com.llastkrakw.nevernote.feature.note.datas.entities.Folder
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import java.util.*

class SearchNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchNoteBinding
    private val noteViewModel : NoteViewModel by viewModels {
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    private lateinit var noteAdapter : NoteAdapter

    private lateinit var addFolderAdapter : AddFolderAdapter
    private lateinit var folderRecyclerView: RecyclerView

    private lateinit var layoutBottomSheet: LinearLayout

    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.title = ""

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

            addFolderAdapter = AddFolderAdapter(noteViewModel, this@SearchNoteActivity)

            folderRecyclerView = addFolderBottomSheet.recyclerFolder
            folderRecyclerView.adapter = addFolderAdapter
            folderRecyclerView.layoutManager = LinearLayoutManager(this@SearchNoteActivity, LinearLayoutManager.VERTICAL, true)

            noteViewModel.allFolderWithNotes.observe(this@SearchNoteActivity,{ folders ->
                folders?.let {
                    addFolderAdapter.submitList(it)
                }
            })

            addFolderBottomSheet.addFolderButton.setOnClickListener {
                showAddFolderDialog()
            }

            layoutBottomSheet = addFolderBottomSheet.folderBottomSheet
            sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            noteViewModel.isClear.observe(this@SearchNoteActivity,  {
                Log.d("clear_bug", "selection was clear is $it")
                if(it)
                    noteAdapter.notifyDataSetChanged()
            })

            noteViewModel.allNoteSelected.observe(this@SearchNoteActivity,  {
                Log.d("clear_bug", "all notes selected $it")
                if(it)
                    noteAdapter.notifyDataSetChanged()
            })
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        noteViewModel.selectedNotes.observe(this, {

            when(it.isEmpty()){
                true -> {
                    binding.myToolbar.visibility = View.GONE
                    invalidateOptionsMenu()
                }
                false -> {
                    binding.myToolbar.visibility = View.VISIBLE
                    menuInflater.inflate(R.menu.selection_menu, menu)
                    invalidateOptionsMenu()
                }
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.action_delete_note ->{
            Log.d("multi", "delete note")
            noteViewModel.deleteNotes()
            true
        }

        R.id.action_select_all_note ->{
            noteViewModel.allNoteSelected.value?.let{
                if (it)
                    noteViewModel.deselectAll()
                else
                    noteViewModel.selectAll()
            }
            Log.d("multi", "selected all")
            true
        }

        R.id.action_folder_note ->{
            toggleBottomSheet()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun showAddFolderDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val folderView = layoutInflater.inflate(R.layout.add_folder, null)

        builder.setView(folderView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editText = folderView.findViewById<EditText>(R.id.add_folder_edit_text)
        val addButton = folderView.findViewById<TextView>(R.id.button_add_folder)
        val cancelButton = folderView.findViewById<TextView>(R.id.add_folder_cancel)

        addButton.setOnClickListener {
            editText.text?.let {
                if(it.toString().isNotEmpty()){
                    val folder = Folder(null, it.toString(), Date())
                    noteViewModel.insertFolder(folder)
                    alertDialog.cancel()
                }
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.cancel()
        }

        alertDialog.show()
    }

    private fun toggleBottomSheet() {
        if (sheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        }
    }

}