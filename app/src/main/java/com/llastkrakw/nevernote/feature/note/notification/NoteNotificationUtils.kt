package com.llastkrakw.nevernote.feature.note.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.utilities.SpanUtils.Companion.toSpannable
import com.llastkrakw.nevernote.feature.note.datas.entities.Note
import com.llastkrakw.nevernote.feature.note.datas.entities.NoteWithFolders
import com.llastkrakw.nevernote.views.notes.activities.NoteDetailActivity

class NoteNotificationUtils {

    companion object{

        @JvmStatic
        fun NotificationManager.sendNotification(applicationContext: Context, note : NoteWithFolders){

            val notificationId = note.note.noteId

            val contentIntent = Intent(applicationContext, NoteDetailActivity::class.java)
            contentIntent.putExtra(NOTIFICATION_NOTE_EXTRA, note)

            val contentPendingIntent = notificationId?.let {
                PendingIntent.getActivity(
                        applicationContext,
                        it,
                        contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

            val builder = NotificationCompat.Builder(
                    applicationContext,
                    applicationContext.getString(R.string.note_channel_id)
            )
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle(applicationContext.getString(R.string.app_name))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(toSpannable(note.note.noteContent)).setBigContentTitle(toSpannable(note.note.noteTitle)))
                    .setContentText(toSpannable(note.note.noteTitle))
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)


            if (notificationId != null) {
                Log.d("notification", "$notificationId")
                builder.priority = NotificationCompat.PRIORITY_HIGH
                notify(notificationId, builder.build())
            }

        }

        @JvmStatic
        fun NotificationManager.cancelNotifications() {
            cancelAll()
        }

    }

}