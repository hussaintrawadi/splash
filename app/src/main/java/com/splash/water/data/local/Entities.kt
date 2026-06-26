package com.splash.water.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** One logged intake event. [dayKey] is the local "yyyy-MM-dd" for fast per-day grouping. */
@Entity(
    tableName = "water_log",
    indices = [Index("dayKey"), Index("timestamp")],
)
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long,
    val dayKey: String,
)

/**
 * An unlocked milestone. [defId] references a static definition in code (MilestoneDefs);
 * [periodKey] is the day ("yyyy-MM-dd") or week ("yyyy-Www") it was earned in, so daily/weekly
 * milestones can be earned again in a new period but only once per period.
 */
@Entity(
    tableName = "achievement",
    indices = [Index(value = ["defId", "periodKey"], unique = true)],
)
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val defId: String,
    val periodKey: String,
    val unlockedAt: Long,
)

/** A manual reminder slot: a repeating interval or a fixed clock time. */
@Entity(tableName = "reminder_slot")
data class ReminderSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** [com.splash.water.domain.model.ReminderKind] name. */
    val kind: String,
    /** For FIXED: minutes since local midnight. Unused for INTERVAL. */
    val minuteOfDay: Int = 0,
    /** For INTERVAL: gap between reminders in minutes. Unused for FIXED. */
    val intervalMinutes: Int = 120,
    val enabled: Boolean = true,
)

/** Projection: total ml for a given day. */
data class DayTotal(
    val dayKey: String,
    val total: Int,
)
