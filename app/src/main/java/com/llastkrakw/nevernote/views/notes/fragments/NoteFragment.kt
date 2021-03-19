package com.llastkrakw.nevernote.views.notes.fragments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.llastkrakw.nevernote.NeverNoteApplication
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.databinding.FragmentNoteBinding
import com.llastkrakw.nevernote.feature.note.adapters.FolderAdapter
import com.llastkrakw.nevernote.feature.note.adapters.NoteAdapter
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModelFactory
import com.llastkrakw.nevernote.views.notes.activities.SearchNoteActivity


class NoteFragment : Fragment() {

    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private var isLinearLayoutManager = true
    private var isNoteNext : Boolean = false

    private val noteViewModel : NoteViewModel by activityViewModels(){
        NoteViewModelFactory((activity?.application as NeverNoteApplication).noteRepository, activity?.application as NeverNoteApplication)
    }

    private lateinit var  noteAdapter : NoteAdapter
    private lateinit var folderAdapter : FolderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentNoteBinding.inflate(layoutInflater, container, false)
        createChannel(
                getString(R.string.note_channel_id),
                getString(R.string.note_channel_name)
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.apply {
            viewModel = noteViewModel
            lifecycleOwner = viewLifecycleOwner
            noteAdapter = NoteAdapter(noteViewModel, viewLifecycleOwner)
            folderAdapter = FolderAdapter(noteViewModel, viewLifecycleOwner)

            recyclerView = noteRecycler
            recyclerView.adapter = noteAdapter

            noteViewModel.allNotesAsc.observe(viewLifecycleOwner, Observer { notes ->
                notes?.let { noteAdapter.submitList(it) }
            })

            noteViewModel.allFolderWithNotes.observe(viewLifecycleOwner, Observer { folders ->
                folders?.let { folderAdapter.submitList(it) }
            })

            noteViewModel.isGrid.observe(viewLifecycleOwner, Observer { isGrid ->
                isLinearLayoutManager = isGrid
                chooseLayoutManager()
            })

            noteViewModel.isCleared.observe(viewLifecycleOwner,  {
                if (it)
                    noteAdapter.notifyDataSetChanged()
            })


            noteToggle.setOnClickListener {
                toggleAdapter()
                verifyNext(this)
            }

            searchNote.setOnClickListener {
                val  intent = Intent(context, SearchNoteActivity::class.java)
                activity?.startActivity(intent)
            }
        }

        //verifyNext(binding)
        chooseLayoutManager()
    }

    private fun verifyNext(binding: FragmentNoteBinding){
        binding.apply {
            if(isNoteNext){
                noteToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_note, 0, 0, 0)
                noteToggle.text = getString(R.string.see_notes)
                noteItemCount.text = String.format(getString(R.string.items), noteViewModel.allFolderWithNotes.value?.size)
            }
            else{
                noteToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_documents_folder, 0, 0, 0)
                noteToggle.text = getString(R.string.see_folders)
                noteItemCount.text = String.format(getString(R.string.items), noteViewModel.allNotesAsc.value?.size)
            }
        }
    }

    private fun chooseLayoutManager() {
        if (!isLinearLayoutManager) {
            recyclerView.layoutManager = LinearLayoutManager(context)
        } else {
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    private fun toggleAdapter(){
        if (isNoteNext){
            recyclerView.adapter = noteAdapter
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            isNoteNext = false
        }
        else{
            recyclerView.adapter = folderAdapter
            recyclerView.layoutManager = LinearLayoutManager(context)
            isNoteNext = true
        }
    }

    private fun createChannel(channelId: String, channelName: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Reminder note"

            val notificationManager = requireActivity().getSystemService(
                    NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }


    companion object {
        @JvmStatic
        fun newInstance() = NoteFragment()
    }

}