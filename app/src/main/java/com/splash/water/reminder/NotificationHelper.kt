package com.splash.water.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.splash.water.R

/** Builds the reminder notification + channels. The sound itself is played by the service. */
object NotificationHelper {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)

        // Active/foreground channel, silent (the service plays the looping ringtone itself, and
        // gates it on the ringer/DND). We deliberately do NOT bypass DND, so it stays quiet on
        // silent / Do Not Disturb, like a normal call would.
        val active = NotificationChannel(
            ReminderConstants.CHANNEL_FOREGROUND,
            "Active reminder",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Shown while a water reminder is ringing"
            setSound(null, null)
            enableVibration(false)
        }

        val reminder = NotificationChannel(
            ReminderConstants.CHANNEL_REMINDER,
            "Water reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Reminders to drink water"
            setSound(null, null)
        }
        nm.createNotificationChannel(active)
        nm.createNotificationChannel(reminder)
    }

    /** The ringing reminder notification: full-screen (call-style) intent + quick actions. */
    fun buildReminderNotification(
        context: Context,
        defaultAmountMl: Int,
        skipCount: Int = 0,
        lastDrink: Long = 0L,
    ): Notification {
        val fullScreen = PendingIntent.getActivity(
            context, ReminderConstants.RC_FULLSCREEN,
            Intent(context, ReminderActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ReminderConstants.EXTRA_AMOUNT, defaultAmountMl)
                .putExtra(ReminderConstants.EXTRA_SKIP_COUNT, skipCount)
                .putExtra(ReminderConstants.EXTRA_LAST_DRINK, lastDrink),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(context, ReminderConstants.CHANNEL_FOREGROUND)
            .setSmallIcon(R.drawable.ic_water_notification)
            .setContentTitle("Time to drink water 💧")
            .setContentText("Open to log a drink, snooze, or dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreen, true)
            .addAction(0, "I drank", actionPi(context, ReminderConstants.ACTION_DRANK, ReminderConstants.RC_DRANK, defaultAmountMl))
            .addAction(0, "Snooze", actionPi(context, ReminderConstants.ACTION_SNOOZE, ReminderConstants.RC_SNOOZE, 0))
            .addAction(0, "Drink later", actionPi(context, ReminderConstants.ACTION_DISMISS, ReminderConstants.RC_DISMISS, 0))
            .build()
    }

    private fun actionPi(context: Context, action: String, requestCode: Int, amountMl: Int): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            if (amountMl > 0) putExtra(ReminderConstants.EXTRA_AMOUNT, amountMl)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
