package com.splash.water.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.splash.water.MainActivity
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.repo.ReminderSlotRepository
import com.splash.water.data.repo.WaterRepository
import com.splash.water.domain.DateUtils
import com.splash.water.domain.ReminderPlanner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the next water reminder as an exact alarm-clock alarm (survives Doze, shows the system
 * alarm icon). Recomputes via [ReminderPlanner] from current prefs + intake + manual slots.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepo: PreferencesRepository,
    private val waterRepo: WaterRepository,
    private val slotRepo: ReminderSlotRepository,
    private val planner: ReminderPlanner,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Recompute and (re)schedule the next reminder. Cancels if none is needed. */
    suspend fun reschedule() {
        val prefs = prefsRepo.preferences.first()
        if (!prefs.remindersEnabled) {
            cancel()
            return
        }
        val total = waterRepo.totalToday()
        val slots = slotRepo.enabledSlots()
        val next = planner.nextReminder(prefs, total, slots, DateUtils.nowMillis())
        if (next == null) cancel() else setAlarm(next)
    }

    /** Schedule a one-off reminder at an absolute time (used for snooze). */
    fun scheduleAt(timeMillis: Long) = setAlarm(timeMillis)

    fun cancel() {
        alarmManager.cancel(firePendingIntent())
    }

    private fun setAlarm(timeMillis: Long) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        val fire = firePendingIntent()
        if (canExact) {
            val show = PendingIntent.getActivity(
                context, ReminderConstants.RC_SHOW,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(timeMillis, show), fire)
        } else {
            // Fallback when exact alarms aren't permitted: inexact but allowed in Doze.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, fire)
        }
    }

    private fun firePendingIntent(): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderConstants.ACTION_FIRE
        }
        return PendingIntent.getBroadcast(
            context, ReminderConstants.RC_FIRE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
