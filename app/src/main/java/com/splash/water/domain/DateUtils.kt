package com.splash.water.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.util.Locale

/** Local-date helpers for grouping intake by day/week/month/year. Uses java.time (desugared). */
object DateUtils {
    private val DAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun zone(): ZoneId = ZoneId.systemDefault()

    fun nowMillis(): Long = System.currentTimeMillis()

    fun today(): LocalDate = LocalDate.now(zone())

    fun dateOf(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalDate()

    fun dayKey(date: LocalDate): String = date.format(DAY_FMT)

    fun dayKey(epochMillis: Long): String = dayKey(dateOf(epochMillis))

    fun todayKey(): String = dayKey(today())

    /** Start-of-day epoch millis for the given date. */
    fun startOfDayMillis(date: LocalDate): Long =
        date.atStartOfDay(zone()).toInstant().toEpochMilli()

    /** "2026-W25" ISO week key. */
    fun weekKey(date: LocalDate): String {
        val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val year = date.get(IsoFields.WEEK_BASED_YEAR)
        return "%d-W%02d".format(year, week)
    }

    /** Monday of the ISO week containing [date]. */
    fun startOfWeek(date: LocalDate): LocalDate =
        date.minusDays(((date.dayOfWeek.value + 6) % 7).toLong())

    fun startOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(1)

    fun startOfYear(date: LocalDate): LocalDate = date.withDayOfYear(1)

    fun shortDayName(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    fun shortMonthName(month: Int): String =
        java.time.Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())

    /** Format an epoch time as a localized clock string like "8:05 AM". */
    fun formatClock(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone()).toLocalTime()
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

    /** Format minutes-since-midnight as a localized clock string like "8:00 AM". */
    fun formatMinuteOfDay(minuteOfDay: Int): String {
        val h = (minuteOfDay / 60) % 24
        val m = minuteOfDay % 60
        return java.time.LocalTime.of(h, m)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }
}
