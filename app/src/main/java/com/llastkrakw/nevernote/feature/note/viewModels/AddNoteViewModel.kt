package com.llastkrakw.nevernote.feature.note.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.llastkrakw.nevernote.core.utilities.Editor


class AddNoteViewModel(private val editor: Editor) : ViewModel() {
}

class AddNoteViewModelFactory(private val editor: Editor) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddNoteViewModelFactory(editor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}