package com.habitflow.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.habitflow.app.data.model.Habit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import android.app.Notification
import com.habitflow.app.R
import kotlinx.coroutines.flow.first

object NotificationHelper {

    const val CHANNEL_ID = "habi_wabi_reminders"
    const val CHANNEL_NAME = "Habit Reminders"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily reminders for your habits"
            enableVibration(true)
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    suspend fun scheduleReminder(context: Context, habit: Habit) {
        if (!habit.reminderEnabled) return

        val prefs = com.habitflow.app.data.PreferencesManager(context)
        val offsetMins = prefs.reminderOffsetMinutes.first()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = buildPendingIntent(context, habit)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, habit.reminderHour)
            set(Calendar.MINUTE, habit.reminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            add(Calendar.MINUTE, -offsetMins)

            // If time already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // setAlarmClock penetrates strict Doze modes and OEM background-process killers (like Samsung)
        val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, intent)
        alarmManager.setAlarmClock(alarmInfo, intent)
    }

    fun scheduleHydrationReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        listOf(9, 21).forEachIndexed { index, hour ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_IS_WATER_REMINDER, true)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                20000 + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val alarmInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, habit: Habit) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context, habit))
    }

    private fun buildPendingIntent(context: Context, habit: Habit): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_HABIT_ID, habit.id)
            putExtra(ReminderReceiver.EXTRA_HABIT_TITLE, habit.title)
        }
        return PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sendWelcomeTestNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Action Intensts (dummy for now, just to dismiss)
        val dismissIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = "DISMISS_WELCOME"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Yay! You're all set 🎉")
            .setContentText("We're just checking if notifications work on your device.")
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_media_play, "Yes, it works!", dismissPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "No", dismissPendingIntent)

        nm.notify(1001, builder.build())
    }
}
