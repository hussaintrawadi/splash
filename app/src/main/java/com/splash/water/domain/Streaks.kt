package com.splash.water.domain

import java.time.LocalDate

/** Streak math from a map of dayKey -> total ml. */
object Streaks {
    /**
     * Current consecutive run of goal-met days ending today (or yesterday if today isn't met yet,
     * so an unfinished day doesn't appear to break the streak).
     */
    fun currentStreak(totalsByDay: Map<String, Int>, goalMl: Int, today: LocalDate = DateUtils.today()): Int {
        if (goalMl <= 0) return 0
        var streak = 0
        val todayMet = (totalsByDay[DateUtils.dayKey(today)] ?: 0) >= goalMl
        var day = if (todayMet) today else today.minusDays(1)
        while ((totalsByDay[DateUtils.dayKey(day)] ?: 0) >= goalMl) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    /** Number of goal-met days within the inclusive range. */
    fun daysMetInRange(
        totalsByDay: Map<String, Int>,
        goalMl: Int,
        start: LocalDate,
        end: LocalDate,
    ): Int {
        var count = 0
        var day = start
        while (!day.isAfter(end)) {
            if ((totalsByDay[DateUtils.dayKey(day)] ?: 0) >= goalMl) count++
            day = day.plusDays(1)
        }
        return count
    }
}
