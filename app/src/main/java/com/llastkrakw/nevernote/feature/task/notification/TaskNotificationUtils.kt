package com.llastkrakw.nevernote.feature.task.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.llastkrakw.nevernote.MainActivity
import com.llastkrakw.nevernote.R
import com.llastkrakw.nevernote.core.constants.IS_NOTIFICATION_TASK_EXTRA
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_NOTE_EXTRA
import com.llastkrakw.nevernote.core.utilities.SpanUtils
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.views.notes.activities.NoteDetailActivity

class TaskNotificationUtils {

    companion object{

        @JvmStatic
        fun NotificationManager.sendNotification(applicationContext: Context, task : Task){

            val notificationId = task.taskId?.toInt()

            val contentIntent = Intent(applicationContext, MainActivity::class.java)
            contentIntent.putExtra(IS_NOTIFICATION_TASK_EXTRA, true)

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
                    applicationContext.getString(R.string.task_channel_id)
            )
                    .setSmallIcon(R.drawable.ic_logo)
                    .setContentTitle(applicationContext.getString(R.string.app_name))
                    .setContentText(task.taskContent)
                    .setContentIntent(contentPendingIntent)
                    .setAutoCancel(true)


            if (notificationId != null) {
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