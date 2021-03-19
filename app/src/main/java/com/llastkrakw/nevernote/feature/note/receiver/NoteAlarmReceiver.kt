package com.llastkrakw.nevernote.feature.note.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.versionedparcelable.ParcelImpl.CREATOR
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.utilities.unmarshall
import com.llastkrakw.nevernote.core.utilities.unmarshallParcelable
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFolders
import com.llastkrakw.nevernote.feature.note.notification.NoteNotificationUtils.Companion.sendNotification


class NoteAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val bytes  = intent.extras?.getByteArray(NOTIFICATION_NOTE_EXTRA)
        val note = bytes?.let { unmarshallParcelable<NoteWithFolders>(it) }




        val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
        ) as NotificationManager

        if (note != null) {
            notificationManager.sendNotification(
                    context,
                    note
            )
        }
        else{
            Log.d("Notification", "note is null")
        }
    }
}