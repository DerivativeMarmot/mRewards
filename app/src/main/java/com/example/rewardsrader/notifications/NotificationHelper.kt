package com.example.rewardsrader.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.rewardsrader.MainActivity
import com.example.rewardsrader.R

class NotificationHelper(private val context: Context) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        ensureChannel()
    }

    fun showNotification(
        notificationId: Int,
        title: String,
        body: String,
        pendingIntent: PendingIntent
    ) {
        val notification = NotificationCompat.Builder(context, TRACKER_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(notificationId, notification)
    }

    fun trackerPendingIntent(trackerId: String): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("rewardsrader://tracker/$trackerId"),
            context,
            MainActivity::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            context,
            trackerScheduleId(trackerId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            TRACKER_REMINDER_CHANNEL_ID,
            TRACKER_REMINDER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for expiring tracker benefits and offers."
        }
        manager.createNotificationChannel(channel)
    }
}
