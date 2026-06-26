package com.splash.water.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.repo.WaterRepository
import com.splash.water.domain.DateUtils
import com.splash.water.domain.MilestoneEngine
import com.splash.water.domain.Streaks
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration

/**
 * Periodic safety net: re-asserts the next alarm (in case the OS dropped it) and evaluates
 * weekly / streak milestones. Runs every few hours.
 */
@HiltWorker
class ReminderSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scheduler: ReminderScheduler,
    private val waterRepo: WaterRepository,
    private val prefsRepo: PreferencesRepository,
    private val milestones: MilestoneEngine,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        scheduler.reschedule()

        val prefs = prefsRepo.preferences.first()
        val totals = waterRepo.observeAllDailyTotals().first().associate { it.dayKey to it.total }
        val today = DateUtils.today()
        val streak = Streaks.currentStreak(totals, prefs.goalMl, today)
        val weekStart = DateUtils.startOfWeek(today)
        val daysMet = Streaks.daysMetInRange(totals, prefs.goalMl, weekStart, today)
        milestones.evaluateWeeklyAndStreak(daysMet, streak, DateUtils.weekKey(today))

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "reminder_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderSyncWorker>(Duration.ofHours(3))
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
