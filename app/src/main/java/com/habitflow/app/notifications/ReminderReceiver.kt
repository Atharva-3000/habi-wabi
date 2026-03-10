package com.habitflow.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.habitflow.app.MainActivity
import com.habitflow.app.R

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HABIT_TITLE = "habit_title"
        const val EXTRA_IS_WATER_REMINDER = "is_water_reminder"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isWater = intent.getBooleanExtra(EXTRA_IS_WATER_REMINDER, false)
        
        if (isWater) {
            NotificationHelper.scheduleHydrationReminders(context)
            
            val tapIntent = PendingIntent.getActivity(
                context,
                20000,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time to hydrate! 💧")
                .setContentText("Keep your body refreshed and your water goal on track.")
                .setContentIntent(tapIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(20000, notification)
            return
        }

        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Your habit"

        val tapIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for: $habitTitle")
            .setContentText("Keep the streak going ✦ One small step today.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Keep the streak going ✦ One small step today.")
            )
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(habitId.toInt(), notification)
    }
}
