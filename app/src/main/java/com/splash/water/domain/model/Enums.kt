package com.splash.water.domain.model

/** Biological sex, used only as a minor input to the hydration estimate. */
enum class Sex { MALE, FEMALE, UNSPECIFIED }

/**
 * Daily activity level; [extraMl] is added to the computed hydration goal to cover sweat losses
 * during exercise (ACSM/NATA guidance suggests roughly +0.4-0.8 L per active hour).
 */
enum class ActivityLevel(val extraMl: Int, val label: String) {
    SEDENTARY(0, "Sedentary"),
    LIGHT(300, "Lightly active"),
    MODERATE(550, "Moderately active"),
    ACTIVE(800, "Very active"),
}

/** Climate; warmer climates add to the goal to account for sweating. */
enum class Climate(val extraMl: Int, val label: String) {
    TEMPERATE(0, "Temperate"),
    WARM(250, "Warm"),
    HOT(500, "Hot / humid"),
}

/** How reminders are scheduled. */
enum class ReminderMode { SMART, MANUAL }

/** A manual reminder slot is either a repeating interval or a fixed clock time. */
enum class ReminderKind { INTERVAL, FIXED }

/** App theme preference. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Result of a reminder being acted upon from the notification / full-screen alarm. */
enum class ReminderAction { DRANK, SNOOZE, DISMISS }
