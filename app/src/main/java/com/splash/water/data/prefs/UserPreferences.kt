package com.splash.water.data.prefs

import com.splash.water.domain.GoalCalculator
import com.splash.water.domain.model.ActivityLevel
import com.splash.water.domain.model.Climate
import com.splash.water.domain.model.ReminderMode
import com.splash.water.domain.model.Sex
import com.splash.water.domain.model.ThemeMode

/** All persisted user settings + profile in one immutable snapshot. */
data class UserPreferences(
    val onboardingComplete: Boolean = false,
    val weightKg: Double? = null,
    val sex: Sex = Sex.UNSPECIFIED,
    val age: Int? = null,
    val activity: ActivityLevel = ActivityLevel.LIGHT,
    val climate: Climate = Climate.TEMPERATE,
    val goalIsAuto: Boolean = true,
    val manualGoalMl: Int = GoalCalculator.DEFAULT_GOAL_ML,
    val wakeStartMinute: Int = 8 * 60,   // 08:00
    val wakeEndMinute: Int = 22 * 60,    // 22:00
    val reminderMode: ReminderMode = ReminderMode.SMART,
    val intervalMinutes: Int = 120,
    val snoozeMinutes: Int = 15,
    val defaultLogMl: Int = 250,
    val remindersEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrate: Boolean = true,
    val soundUri: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Consecutive "drink later" skips since the last logged intake. */
    val skipCount: Int = 0,
) {
    /** The effective daily goal: auto-computed from body stats, or the manual override. */
    val goalMl: Int
        get() = if (goalIsAuto) {
            GoalCalculator.computeGoal(weightKg, sex, age, activity, climate)
        } else {
            GoalCalculator.clamp(manualGoalMl)
        }
}
