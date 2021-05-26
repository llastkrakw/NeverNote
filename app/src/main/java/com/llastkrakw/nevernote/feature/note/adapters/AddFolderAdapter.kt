package com.llastkrakw.nevernote.feature.note.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.feature.note.datas.entities.FolderWithNotes
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.views.notes.activities.FolderDetailActivity
import java.util.*

class AddFolderAdapter(private val noteViewModel: NoteViewModel, private val owner: LifecycleOwner) : ListAdapter<FolderWithNotes, AddFolderAdapter.FolderViewHolder>(FoldersComparator()) {

    companion object{
        const val EXTRA_FOLDER = "com.llastkrakw.nevernote.entitie.folder"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder.create(parent, noteViewModel, owner)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val currentFolder = getItem(position)
        holder.bind(currentFolder)
    }

    class FolderViewHolder(itemView: View, private val noteViewModel: NoteViewModel, private val owner: LifecycleOwner) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val folderTitle: TextView = itemView.findViewById(R.id.folder_title)
        private lateinit var currentFolderWithNotes: FolderWithNotes

        fun bind(folder: FolderWithNotes) {
            currentFolderWithNotes = folder
            folderTitle.text = String.format(itemView.resources.getString(R.string.folder_title), currentFolderWithNotes.folder.folderName, currentFolderWithNotes.notes.size)

            itemView.setOnClickListener(this)
        }

        companion object {
            fun create(parent: ViewGroup, noteViewModel: NoteViewModel, owner: LifecycleOwner): FolderViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.folder_item_bs, parent, false)
                return FolderViewHolder(view, noteViewModel, owner)
            }
        }

        override fun onClick(v: View?) {
            noteViewModel.addNotesToFolder(currentFolderWithNotes)
        }

    }


    class FoldersComparator : DiffUtil.ItemCallback<FolderWithNotes>() {
        override fun areItemsTheSame(oldItem: FolderWithNotes, newItem: FolderWithNotes): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: FolderWithNotes, newItem: FolderWithNotes): Boolean {
            return oldItem.folder.folderName == newItem.folder.folderName
        }
    }
}