package com.splash.water.domain

import com.splash.water.data.repo.AchievementRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides which milestones are newly unlocked and persists them (once per period).
 * Returns the freshly-unlocked definitions so the UI can reveal a celebration card.
 */
@Singleton
class MilestoneEngine @Inject constructor(
    private val achievementRepo: AchievementRepository,
) {
    /**
     * Call after each log with the new daily total and the active goal. Daily steps are
     * percentage-of-goal (25/50/75/100/125%), so they unlock for any goal. Returns the steps
     * freshly crossed today so the home screen can pop a celebration card.
     */
    suspend fun evaluateDaily(currentMl: Int, goalMl: Int): List<MilestoneDef> {
        if (goalMl <= 0) return emptyList()
        val periodKey = DateUtils.todayKey()
        val newly = mutableListOf<MilestoneDef>()
        for (step in DailyStep.entries) {
            if (currentMl >= step.thresholdMl(goalMl) && achievementRepo.unlock(step.toMilestoneDef().id, periodKey)) {
                newly += step.toMilestoneDef()
            }
        }
        return newly
    }

    /** Call at day/week rollover with goal-met day count and the current streak length. */
    suspend fun evaluateWeeklyAndStreak(daysMetThisWeek: Int, streak: Int, weekKey: String): List<MilestoneDef> {
        val newly = mutableListOf<MilestoneDef>()
        for (def in MilestoneDefs.weekly) {
            if (daysMetThisWeek >= def.threshold && achievementRepo.unlock(def.id, weekKey)) newly += def
        }
        for (def in MilestoneDefs.streak) {
            // Streak milestones are unlocked once per reaching (keyed by threshold milestone hit count window).
            if (streak >= def.threshold && achievementRepo.unlock(def.id, "streak")) newly += def
        }
        return newly
    }
}
