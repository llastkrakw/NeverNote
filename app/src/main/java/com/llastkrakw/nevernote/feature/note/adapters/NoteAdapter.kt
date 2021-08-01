package com.llastkrakw.nevernote.feature.note.adapters

import android.content.Intent
import android.content.res.TypedArray
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.MAX_CONTENT
import com.llastkrakw.nevernote.core.constants.MAX_TITLE
import com.llastkrakw.nevernote.core.utilities.FormatUtils.Companion.toSimpleString
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.toSpannable
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFoldersAndRecords
import com.llastkrakw.nevernote.feature.note.viewModels.NoteViewModel
import com.llastkrakw.nevernote.views.notes.activities.NoteDetailActivity
import java.util.*


class NoteAdapter(private val noteViewModel: NoteViewModel, private val owner: LifecycleOwner) : ListAdapter<NoteWithFoldersAndRecords, NoteAdapter.NoteViewHolder>(NotesComparator()) {

    companion object{
        const val NOTE_EXTRA = "com.llastkrakw.nevernote.note.adapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder.create(parent, noteViewModel, owner)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class NoteViewHolder(itemView: View, private val noteViewModel: NoteViewModel, private val owner: LifecycleOwner)
        : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener{

        private lateinit var  currentNote : NoteWithFoldersAndRecords
        private var colors: TypedArray = itemView.resources.obtainTypedArray(R.array.random_color)
        var color = colors.getColor(Random().nextInt(8), 0)

        private val noteCard: CardView = itemView.findViewById(R.id.note_card)

        private val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        private val noteContent: TextView = itemView.findViewById(R.id.note_content)
        private val noteDate: TextView = itemView.findViewById(R.id.note_date)
        private val check: ImageView = itemView.findViewById(R.id.isChecked)
        private val haveAudio: ImageButton = itemView.findViewById(R.id.have_audio)
        private val haveClock: ImageButton = itemView.findViewById(R.id.have_clock)

        fun bind(note: NoteWithFoldersAndRecords) {
            currentNote = note
            val title = toSpannable(note.note.noteTitle)
            val content = toSpannable(note.note.noteContent)
            val date = note.note.noteCreatedAt
            noteTitle.text = title
            if (content.toString().length > MAX_CONTENT){
                noteContent.text = String.format("%s...", content.subSequence(0, MAX_CONTENT))
            }
            else
                noteContent.text = content.toString()

            if (title.toString().length > MAX_TITLE){
                noteTitle.text = String.format("%s... \n", title.subSequence(0, MAX_TITLE))
            }
            else
                noteTitle.text = title.toString()

            noteCard.setCardBackgroundColor(color)
            noteDate.text = toSimpleString(date)

            haveAudio.visibility = if(note.recordsRef.isNotEmpty()) View.VISIBLE else View.GONE
            haveClock.visibility = if(note.note.noteReminder != null) View.VISIBLE else View.GONE

            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)

            noteViewModel.selectedNotes.observe(owner,  {
                if (it.isEmpty())
                    check.visibility = View.GONE
                else if (it.contains(note.note))
                    check.visibility = View.VISIBLE
            })
        }



        companion object {
            fun create(parent: ViewGroup, noteViewModel: NoteViewModel, owner: LifecycleOwner): NoteViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.note_item, parent, false)
                return NoteViewHolder(view, noteViewModel, owner)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (v != null) {
                Log.d("multi", owner.toString())
                noteViewModel.selectedNotes.observe(owner, {

                    if(it.contains(currentNote.note)){
                        Log.d("multi", "note clicked ${currentNote.note.noteId}")
                        check.visibility = View.GONE
                        noteViewModel.deselectNote(currentNote.note)
                    }
                    else{
                        Log.d("multi", "note clicked ${currentNote.note.noteId}")
                        check.visibility = View.VISIBLE
                        noteViewModel.selectNote(currentNote.note)
                    }
                })
            }
            return true
        }

        override fun onClick(v: View?) {
            val intentDetail = Intent(v?.context, NoteDetailActivity::class.java)

            noteViewModel.selectedNotes.observe(owner, {
                if(it.isNotEmpty()){
                    Log.d("multi", it.toString())
                    if(it.contains(currentNote.note)){
                        Log.d("multi", "note clicked ${currentNote.note.noteId}")
                        check.visibility = View.GONE
                        noteViewModel.deselectNote(currentNote.note)
                    }
                    else{
                        Log.d("multi", "note clicked ${currentNote.note.noteId}")
                        check.visibility = View.VISIBLE
                        noteViewModel.selectNote(currentNote.note)
                    }
                }
                else{
                    Log.d("categorize", "notes detail")
                    intentDetail.putExtra(NOTE_EXTRA, currentNote)
                    v?.context?.startActivity(intentDetail)
                }
            })

        }
    }


    class NotesComparator : DiffUtil.ItemCallback<NoteWithFoldersAndRecords>() {
        override fun areItemsTheSame(oldItem: NoteWithFoldersAndRecords, newItem: NoteWithFoldersAndRecords): Boolean {
            return oldItem.note.noteId == newItem.note.noteId
        }

        override fun areContentsTheSame(
            oldItem: NoteWithFoldersAndRecords,
            newItem: NoteWithFoldersAndRecords
        ): Boolean {
            return oldItem.note.noteContent == newItem.note.noteContent
        }


    }



    fun performFiltering(constraint: CharSequence?, completeList : List<NoteWithFoldersAndRecords>){

        val filteredList = mutableListOf<NoteWithFoldersAndRecords>()

        if (constraint == null || constraint.isEmpty()) {
            submitList(completeList)
        }
        else {
            for (item in completeList) {
                if (item.note.noteContent.lowercase(Locale.ROOT).contains(constraint.toString()
                        .lowercase(Locale.ROOT)) || item.note.noteTitle.lowercase(Locale.ROOT)
                        .contains(constraint.toString().lowercase(Locale.ROOT))) {
                    filteredList.add(item)
                }
            }

            submitList(filteredList)
        }
    }

}