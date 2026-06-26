package com.splash.water.reminder

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.repo.WaterRepository
import com.splash.water.domain.MilestoneEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Handles the notification / full-screen action buttons: I drank / Snooze / Not now. */
@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject lateinit var waterRepo: WaterRepository
    @Inject lateinit var prefsRepo: PreferencesRepository
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject lateinit var milestones: MilestoneEngine

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val amount = intent.getIntExtra(ReminderConstants.EXTRA_AMOUNT, 0)
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                // Always stop the ringing first.
                ReminderSoundService.stop(context)
                context.getSystemService(NotificationManager::class.java)
                    ?.cancel(ReminderConstants.FOREGROUND_NOTIF_ID)

                when (action) {
                    ReminderConstants.ACTION_DRANK -> {
                        if (amount > 0) {
                            waterRepo.logWater(amount)
                            val prefs = prefsRepo.preferences.first()
                            milestones.evaluateDaily(waterRepo.totalToday(), prefs.goalMl)
                        }
                        prefsRepo.resetSkipCount()
                        scheduler.reschedule()
                    }
                    ReminderConstants.ACTION_SNOOZE -> {
                        val prefs = prefsRepo.preferences.first()
                        scheduler.scheduleAt(
                            System.currentTimeMillis() + prefs.snoozeMinutes * 60_000L
                        )
                    }
                    ReminderConstants.ACTION_DISMISS -> {
                        prefsRepo.incrementSkipCount()
                        scheduler.reschedule()
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
