package com.llastkrakw.nevernote.views.notes.activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.BACK_SONG
import com.llastkrakw.nevernote.core.constants.DELETE_SONG
import com.llastkrakw.nevernote.core.constants.SUCCESS_SONG
import com.llastkrakw.nevernote.core.constants.TAP_SONG
import com.llastkrakw.nevernote.core.extension.playUiSong
import com.llastkrakw.nevernote.core.extension.toast
import com.llastkrakw.nevernote.databinding.ActivityFolderDetailBinding
import com.llastkrakw.nevernote.feature.note.adapters.AddFolderAdapter
import com.llastkrakw.nevernote.feature.note.adapters.FolderAdapter.Companion.EXTRA_FOLDER
import com.llastkrakw.nevernote.feature.note.adapters.OtherNoteAdapter
import com.llastkrakw.nevernote.feature.note.datas.entities.Folder
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderWithNotes
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import java.text.DateFormat
import java.util.*

class FolderDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFolderDetailBinding
    private var folderWithNotes: FolderWithNotes? = null

    private val noteViewModel : NoteViewModel by viewModels {
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    private lateinit var addFolderAdapter : AddFolderAdapter
    private lateinit var  noteAdapter : OtherNoteAdapter

    private lateinit var folderRecyclerView: RecyclerView
    private lateinit var layoutBottomSheet: LinearLayout
    private lateinit var sheetBehavior: BottomSheetBehavior<*>


    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        binding = ActivityFolderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        folderWithNotes = intent?.getParcelableExtra(EXTRA_FOLDER)
        noteAdapter = OtherNoteAdapter(noteViewModel, this)

        setSupportActionBar(binding.myToolbar)
        supportActionBar?.let {
            title = ""
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        if (folderWithNotes != null)
            setUi(binding)
    }

    private fun setUi(binding: ActivityFolderDetailBinding) {

        folderWithNotes!!.let {

            binding.apply {

                val currentDate = it.folder.folderCreatedAt
                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(currentDate)
                folderDateSize.text = String.format("%s", dateFormat)

                folderNotes.text = String.format("have %s notes", it.notes.size)

                folderTitle.text = it.folder.folderName

                noteRecycler.adapter = noteAdapter
                noteRecycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

                noteAdapter.submitList(it.notes)

                nothingToShow.visibility = if(it.notes.isNotEmpty()) View.GONE else View.VISIBLE

/*                noteViewModel.isClear.observe(this@FolderDetailActivity,  {
                    Log.d("clear_bug", "selection was clear is $it")
                    if(it)
                        noteAdapter.notifyDataSetChanged()
                })

                noteViewModel.allNoteSelected.observe(this@FolderDetailActivity,  {
                    Log.d("clear_bug", "all notes selected $it")
                    if(it)
                        noteAdapter.notifyDataSetChanged()
                })*/

                addFolderAdapter = AddFolderAdapter(noteViewModel, this@FolderDetailActivity)

                folderRecyclerView = addFolderBottomSheet.recyclerFolder
                folderRecyclerView.adapter = addFolderAdapter
                folderRecyclerView.layoutManager = LinearLayoutManager(this@FolderDetailActivity, LinearLayoutManager.VERTICAL, true)

                noteViewModel.allFolderWithNotes.observe(this@FolderDetailActivity,{ folders ->
                    folders?.let {
                        addFolderAdapter.submitList(it)
                    }
                })


                addFolderBottomSheet.addFolderButton.setOnClickListener {
                    this@FolderDetailActivity.playUiSong(TAP_SONG)
                    showAddFolderDialog()
                }

                layoutBottomSheet = addFolderBottomSheet.folderBottomSheet
                sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
                sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

                noteViewModel.isClear.observe(this@FolderDetailActivity,  {
                    Log.d("clear_bug", "selection was clear is $it")
                    if(it)
                        noteAdapter.notifyDataSetChanged()
                })

                noteViewModel.allNoteSelected.observe(this@FolderDetailActivity,  {
                    Log.d("clear_bug", "all notes selected $it")
                    if(it)
                        noteAdapter.notifyDataSetChanged()
                })

            }

        }

    }

    override fun onSupportNavigateUp(): Boolean {
        this.playUiSong(BACK_SONG)
        onBackPressed()
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        noteViewModel.selectedNotes.observe(this, {

            when(it.isEmpty()){
                true -> {
                    menuInflater.inflate(R.menu.folder_detail_menu, menu)
                    invalidateOptionsMenu()
                }
                false -> {
                    menuInflater.inflate(R.menu.selection_menu, menu)
                    invalidateOptionsMenu()
                }
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.action_delete_folder ->{
            this.playUiSong(DELETE_SONG)
            folderWithNotes?.let { noteViewModel.deleteFolder(it.folder) }
            onBackPressed()
            finish()
            true
        }

        R.id.action_delete_note ->{
            this.playUiSong(DELETE_SONG)
            Log.d("multi", "delete note")
            noteViewModel.selectedNotes.observe(this, { selectedNotes ->
                noteAdapter.submitList(folderWithNotes!!.notes.filter {
                    return@filter !(selectedNotes.contains(it))
                })
            })
            noteViewModel.deleteNotes()
            true
        }

        R.id.action_select_all_note ->{
/*            noteViewModel.allNoteSelected.value?.let{
                if (it)
                    noteViewModel.deselectAll()
                else
                    noteViewModel.selectAll()
            }*/
            this.playUiSong(TAP_SONG)
            noteViewModel.selectAll()
            Log.d("multi", "selected all")
            true
        }

        R.id.action_folder_note ->{
            this.playUiSong(DELETE_SONG)
            toggleBottomSheet()
            true
        }

        else ->{
             super.onOptionsItemSelected(item)
        }
    }


    private fun toggleBottomSheet() {
        if (sheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
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
            this.playUiSong(TAP_SONG)
            editText.text?.let {
                if(it.toString().isNotEmpty()){
                    val folder = Folder(null, it.toString(), Date())
                    noteViewModel.insertFolder(folder)
                    this.playUiSong(SUCCESS_SONG)
                    alertDialog.cancel()
                }
                else{
                    toast("You can't add empty folder !")
                }
            }
        }

        cancelButton.setOnClickListener {
            this.playUiSong(TAP_SONG)
            alertDialog.cancel()
        }

        alertDialog.show()
    }
}