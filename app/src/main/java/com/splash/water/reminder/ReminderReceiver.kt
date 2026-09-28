package com.splash.water.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.repo.WaterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fires when an alarm triggers: starts the looping sound service (which posts the call-style
 * notification) and launches the full-screen reminder directly. Because the alarm is scheduled
 * with setAlarmClock(), this broadcast is granted a short background-activity-start window, so the
 * call screen appears whether the phone is locked or unlocked. The notification's full-screen
 * intent is kept as a fallback for locked-down OEMs.
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var prefsRepo: PreferencesRepository
    @Inject lateinit var waterRepo: WaterRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderConstants.ACTION_FIRE) return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                val prefs = prefsRepo.preferences.first()
                if (!prefs.remindersEnabled) return@launch
                val lastDrink = waterRepo.lastLogTime() ?: 0L

                ReminderSoundService.start(
                    context = context,
                    amountMl = prefs.defaultLogMl,
                    soundEnabled = prefs.soundEnabled,
                    vibrate = prefs.vibrate,
                    soundUri = prefs.soundUri,
                    skipCount = prefs.skipCount,
                    lastDrink = lastDrink,
                )

                // Launch the call screen directly (alarm-clock grant covers the background start).
                val activity = Intent(context, ReminderActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(ReminderConstants.EXTRA_AMOUNT, prefs.defaultLogMl)
                    .putExtra(ReminderConstants.EXTRA_SKIP_COUNT, prefs.skipCount)
                    .putExtra(ReminderConstants.EXTRA_LAST_DRINK, lastDrink)
                runCatching { context.startActivity(activity) }

                // Queue the NEXT reminder right now, so ignoring this one never stalls the chain.
                runCatching { scheduler.reschedule() }
            } finally {
                pending.finish()
            }
        }
    }
}
