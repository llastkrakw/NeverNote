package com.llastkrakw.nevernote.views.notes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.activity.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.databinding.ActivityFolderDetailBinding
import com.llastkrakw.nevernote.feature.note.adapters.FolderAdapter.Companion.EXTRA_FOLDER
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter
import com.llastkrakw.nevernote.feature.note.adapters.OtherNoteAdapter
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderWithNotes
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import java.text.DateFormat

class FolderDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFolderDetailBinding
    private var folderWithNotes: FolderWithNotes? = null

    private val noteViewModel : NoteViewModel by viewModels(){
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

            }

        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.folder_detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.action_delete_folder ->{
            folderWithNotes?.let { noteViewModel.deleteFolder(it.folder) }
            onBackPressed()
            finish()
            true
        }

        else ->{
             super.onOptionsItemSelected(item)
        }
    }
}