package com.llastkrakw.nevernote.feature.task.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.llastkrakw.nevernote.core.constants.NOTIFICATION_TASK_EXTRA
import com.llastkrakw.nevernote.core.utilities.unmarshallParcelable
import com.llastkrakw.nevernote.feature.task.datas.entities.Task
import com.llastkrakw.nevernote.feature.task.notification.TaskNotificationUtils.Companion.sendNotification

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val bytes  = intent.extras?.getByteArray(NOTIFICATION_TASK_EXTRA)
        val task = bytes?.let { unmarshallParcelable<Task>(it) }



        val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
        ) as NotificationManager

        if (task != null) {
            notificationManager.sendNotification(
                    context,
                    task
            )
        }
        else{
            Log.d("Notification", "task is null")
        }
    }
}