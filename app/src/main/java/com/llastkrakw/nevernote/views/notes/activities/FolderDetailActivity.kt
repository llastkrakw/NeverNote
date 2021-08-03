package com.llastkrakw.nevernote.views.notes.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.extension.toast
import com.llastkrakw.nevernote.databinding.ActivityFolderDetailBinding
import com.llastkrakw.nevernote.feature.note.adapters.FolderAdapter.Companion.EXTRA_FOLDER
import com.llastkrakw.nevernote.feature.note.adapters.OtherNoteAdapter
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderWithNotes
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import java.text.DateFormat

class FolderDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFolderDetailBinding
    private var folderWithNotes: FolderWithNotes? = null

    private val noteViewModel : NoteViewModel by viewModels {
        NoteViewModelFactory((application as NeverNoteApplication).noteRepository, application)
    }

    private lateinit var  noteAdapter : OtherNoteAdapter

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

            }

        }

    }

    override fun onSupportNavigateUp(): Boolean {
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
            folderWithNotes?.let { noteViewModel.deleteFolder(it.folder) }
            onBackPressed()
            finish()
            true
        }

        R.id.action_delete_note ->{
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
            noteViewModel.selectAll()
            Log.d("multi", "selected all")
            true
        }

        R.id.action_folder_note ->{
            toast("now you cannot do this there")
            true
        }

        else ->{
             super.onOptionsItemSelected(item)
        }
    }
}