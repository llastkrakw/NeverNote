package com.llastkrakw.nevernote.feature.note.viewModels

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.*
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_REQUEST_CODE
import com.llastkrakw.nevernote.core.utilities.marshall
import com.llastkrakw.nevernote.core.utilities.marshallParcelable
import com.llastkrakw.nevernote.feature.note.datas.entities.*
import com.llastkrakw.nevernote.feature.note.receiver.NoteAlarmReceiver
import com.llastkrakw.nevernote.feature.note.repositories.NoteRepository
import kotlinx.coroutines.*

class NoteViewModel(private val noteRepository: NoteRepository, private val app: Application) : ViewModel() {

    private val _isGrid = MutableLiveData<Boolean>(true)
    val isGrid: LiveData<Boolean> = _isGrid

    private val _isCleared = MutableLiveData<Boolean>(false)
    val isCleared: LiveData<Boolean> = _isCleared

    private val _selectedNotes = MutableLiveData<MutableList<Note>>(mutableListOf())
    val selectedNotes : LiveData<MutableList<Note>> = _selectedNotes

    val allNotesAsc : LiveData<List<Note>> = noteRepository.allNotesAsc.asLiveData()
    val allNotesAscWithFolders : LiveData<List<NoteWithFolders>> = noteRepository.allNotesAscWithFolders.asLiveData()
    val allNotesLastUpdate : LiveData<List<Note>> = noteRepository.allNotesLastUpdate.asLiveData()

    val allFolderWithNotes : LiveData<List<FolderWithNotes>> = noteRepository.allFolderWithNotes.asLiveData()
    val allFolder : LiveData<List<Folder>> = noteRepository.allFolder.asLiveData()


    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun toggleLayoutNoteManager(value: Boolean){
        _isGrid.value = value
    }

    /*
    *
    * Note
    *
    * */

    fun insertNote(note: Note) = viewModelScope.launch {
        noteRepository.insertNote(note)
    }


    fun deleteNotes() = viewModelScope.launch {
        selectedNotes.value?.let { noteRepository.deleteNotes(it.toList()) }
        _selectedNotes.value?.clear()
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteNote(note)
    }

    fun selectNote(note: Note){
        Log.d("multi", "note selected ${note.noteBg}")
        _selectedNotes.value?.let {
            if(!it.contains(note))
                it.add(note)
            Log.d("multi", "notes ${it.size}")
        }
        verifyCleared()
    }

    fun deselectNote(note: Note){
        Log.d("multi", "note deselected ${note.noteBg}")
        _selectedNotes.value?.let {
            if(it.contains(note))
                it.remove(note)
            Log.d("multi", "notes ${it.size}")
        }
        verifyCleared()
    }

    private fun verifyCleared(){
        _isCleared.value = _selectedNotes.value?.isEmpty() == true
    }

    fun addNoteReminder(note: NoteWithFolders, selectedTime : Long) = viewModelScope.launch {

        val notifyIntent = Intent(app, NoteAlarmReceiver::class.java)

        val bytes: ByteArray = marshallParcelable(note)

        notifyIntent.putExtra(NOTIFICATION_NOTE_EXTRA, bytes)

        val notifyPendingIntent = PendingIntent.getBroadcast(
                app,
                NOTIFICATION_NOTE_REQUEST_CODE,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

/*        val notificationManager = ContextCompat.getSystemService(
                app,
                NotificationManager::class.java
        ) as NotificationManager
        notificationManager.sendNotification(app, note)*/

        AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                selectedTime,
                notifyPendingIntent
        )
    }

    /*
    *
    * Folder
    *
    * */


    fun insertFolder(folder: Folder) = viewModelScope.launch {
        noteRepository.insertFolder(folder)
    }

    fun updateFolder(folder: Folder) = viewModelScope.launch {
        noteRepository.updateFolder(folder)
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        noteRepository.deleteFolder(folder)
    }


    fun addNotesToFolder(folder: FolderWithNotes) = viewModelScope.launch {
        selectedNotes.value?.forEach { note ->
            if(note.noteId != null && folder.folder.folderId != null)
                if (!folder.notes.contains(note))
                    noteRepository.insertCrossRef(FolderNoteCrossRef(folder.folder.folderId, note.noteId!!))
        }
        _selectedNotes.value?.clear()
        verifyCleared()
    }



}

class NoteViewModelFactory(private val noteRepository: NoteRepository, private val app: Application) : ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(noteRepository, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}