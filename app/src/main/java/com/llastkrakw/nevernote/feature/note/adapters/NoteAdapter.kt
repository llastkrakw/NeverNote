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
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.MAX_CONTENT
import com.llastkrakw.nevernote.core.constants.MAX_TITLE
import com.llastkrakw.nevernote.core.constants.SELECTION_SONG
import com.llastkrakw.nevernote.core.constants.TAP_SONG
import com.llastkrakw.nevernote.core.extension.dateExpired
import com.llastkrakw.nevernote.core.extension.playUiSong
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class NoteViewHolder(itemView: View, private val noteViewModel: NoteViewModel, private val owner: LifecycleOwner)
        : RecyclerView.ViewHolder(itemView), View.OnLongClickListener, View.OnClickListener{

        private lateinit var  currentNote : NoteWithFoldersAndRecords
        private var colors: TypedArray = itemView.resources.obtainTypedArray(R.array.random_color)
        private val colorId = Random().nextInt(8)
        var color = colors.getColor(colorId, 0)

        private val noteCard: CardView = itemView.findViewById(R.id.note_card)

        private val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        private val noteContent: TextView = itemView.findViewById(R.id.note_content)
        private val noteDate: TextView = itemView.findViewById(R.id.note_date)
        private val check: ImageView = itemView.findViewById(R.id.isChecked)
        private val haveAudio: ImageButton = itemView.findViewById(R.id.have_audio)
        private val haveClock: ImageButton = itemView.findViewById(R.id.have_clock)
        private val haveBg: ImageButton = itemView.findViewById(R.id.have_bg)

        @RequiresApi(Build.VERSION_CODES.O)
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

            if(note.note.noteColor == null){
                noteCard.setCardBackgroundColor(color)
                note.note.noteColor = colorId
                noteViewModel.updateNote(note.note)
            }
            else{
                noteCard.setCardBackgroundColor(colors.getColor(note.note.noteColor!!, 0))
            }
            noteDate.text = toSimpleString(date)

            haveAudio.visibility = if(note.recordsRef.isNotEmpty()) View.VISIBLE else View.GONE
            haveClock.visibility = if(note.note.noteReminder != null && !note.note.noteReminder!!.dateExpired()) View.VISIBLE else View.GONE
            haveBg.visibility = if(note.note.noteBg != null) View.VISIBLE else View.GONE

            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)

            noteViewModel.selectedNotes.observe(owner,  {
                if (it.contains(currentNote.note))
                    check.visibility = View.VISIBLE
                else
                    check.visibility = View.GONE
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
                v.context?.playUiSong(SELECTION_SONG)
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
                        v?.context?.playUiSong(TAP_SONG)
                        Log.d("multi", "note clicked ${currentNote.note.noteId}")
                        check.visibility = View.GONE
                        noteViewModel.deselectNote(currentNote.note)
                    }
                    else{
                        v?.context?.playUiSong(SELECTION_SONG)
                        Log.d("multi", "note clicked ${currentNote.note.noteId}")
                        check.visibility = View.VISIBLE
                        noteViewModel.selectNote(currentNote.note)
                    }
                }
                else{
                    v?.context?.playUiSong(TAP_SONG)
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