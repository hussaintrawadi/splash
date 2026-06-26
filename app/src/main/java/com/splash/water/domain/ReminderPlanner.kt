package com.splash.water.domain

import com.splash.water.data.local.ReminderSlotEntity
import com.splash.water.data.prefs.UserPreferences
import com.splash.water.domain.model.ReminderKind
import com.splash.water.domain.model.ReminderMode
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure logic that decides *when* the next reminder should fire. No Android dependencies, so it's
 * easy to reason about and test. Returns epoch millis, or null when nothing should be scheduled.
 */
@Singleton
class ReminderPlanner @Inject constructor() {

    /** Minimum gap so smart mode never spams reminders back-to-back. */
    private val minGapMinutes = 30L

    fun nextReminder(
        prefs: UserPreferences,
        todayTotalMl: Int,
        slots: List<ReminderSlotEntity>,
        nowMillis: Long,
    ): Long? {
        if (!prefs.remindersEnabled) return null
        return when (prefs.reminderMode) {
            ReminderMode.SMART -> smartNext(prefs, todayTotalMl, nowMillis)
            ReminderMode.MANUAL -> manualNext(prefs, slots, nowMillis)
        }
    }

    private fun smartNext(prefs: UserPreferences, todayTotalMl: Int, nowMillis: Long): Long? {
        val today = DateUtils.today()
        val tomorrow = today.plusDays(1)
        val nowMin = minuteOfDay(nowMillis)

        // Before the day starts → first reminder at wake start.
        if (nowMin < prefs.wakeStartMinute) return epochAt(today, prefs.wakeStartMinute)

        // Goal already met, or the waking window is over → resume tomorrow morning.
        val goal = prefs.goalMl
        if (todayTotalMl >= goal || nowMin >= prefs.wakeEndMinute) {
            return epochAt(tomorrow, prefs.wakeStartMinute)
        }

        // Spread the remaining drinks across the rest of the waking window.
        val remainingMl = (goal - todayTotalMl).coerceAtLeast(0)
        val drinks = Math.ceil(remainingMl.toDouble() / prefs.defaultLogMl.coerceAtLeast(50)).toInt()
            .coerceAtLeast(1)
        val remainingWindowMin = (prefs.wakeEndMinute - nowMin).coerceAtLeast(1)
        val gapMin = (remainingWindowMin / drinks).toLong().coerceAtLeast(minGapMinutes)
        val nextMin = nowMin + gapMin.toInt()

        return if (nextMin > prefs.wakeEndMinute) {
            epochAt(tomorrow, prefs.wakeStartMinute)
        } else {
            epochAt(today, nextMin)
        }
    }

    private fun manualNext(prefs: UserPreferences, slots: List<ReminderSlotEntity>, nowMillis: Long): Long? {
        if (slots.isEmpty()) return null
        val today = DateUtils.today()
        val tomorrow = today.plusDays(1)
        val candidates = mutableListOf<Long>()

        for (slot in slots) {
            when (ReminderKind.valueOf(slot.kind)) {
                ReminderKind.FIXED -> {
                    val t = epochAt(today, slot.minuteOfDay)
                    candidates += if (t > nowMillis) t else epochAt(tomorrow, slot.minuteOfDay)
                }
                ReminderKind.INTERVAL -> {
                    nextIntervalTick(prefs, slot.intervalMinutes, nowMillis)?.let { candidates += it }
                }
            }
        }
        return candidates.minOrNull()
    }

    /** Next interval tick aligned to wake start, within the waking window (else next morning). */
    private fun nextIntervalTick(prefs: UserPreferences, intervalMinutes: Int, nowMillis: Long): Long {
        val interval = intervalMinutes.coerceAtLeast(15)
        val today = DateUtils.today()
        val tomorrow = today.plusDays(1)
        val nowMin = minuteOfDay(nowMillis)

        if (nowMin < prefs.wakeStartMinute) return epochAt(today, prefs.wakeStartMinute)
        if (nowMin >= prefs.wakeEndMinute) return epochAt(tomorrow, prefs.wakeStartMinute)

        val sinceStart = nowMin - prefs.wakeStartMinute
        val ticks = sinceStart / interval + 1
        val nextMin = prefs.wakeStartMinute + ticks * interval
        return if (nextMin > prefs.wakeEndMinute) epochAt(tomorrow, prefs.wakeStartMinute)
        else epochAt(today, nextMin)
    }

    private fun minuteOfDay(epochMillis: Long): Int {
        val t = java.time.Instant.ofEpochMilli(epochMillis).atZone(DateUtils.zone()).toLocalTime()
        return t.hour * 60 + t.minute
    }

    private fun epochAt(date: LocalDate, minuteOfDay: Int): Long {
        val clamped = minuteOfDay.coerceIn(0, 24 * 60 - 1)
        val time = LocalTime.of(clamped / 60, clamped % 60)
        return date.atTime(time).atZone(DateUtils.zone()).toInstant().toEpochMilli()
    }
}
