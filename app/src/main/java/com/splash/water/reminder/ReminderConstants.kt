package com.splash.water.reminder

/** Shared constants for the reminder subsystem: channels, intent actions, request codes. */
object ReminderConstants {
    const val CHANNEL_REMINDER = "water_reminder"
    const val CHANNEL_FOREGROUND = "water_reminder_active"

    const val NOTIF_ID_REMINDER = 1001
    const val FOREGROUND_NOTIF_ID = 1002

    // Broadcast actions
    const val ACTION_FIRE = "com.splash.water.action.FIRE"
    const val ACTION_DRANK = "com.splash.water.action.DRANK"
    const val ACTION_SNOOZE = "com.splash.water.action.SNOOZE"
    const val ACTION_DISMISS = "com.splash.water.action.DISMISS"
    const val ACTION_STOP_SOUND = "com.splash.water.action.STOP_SOUND"

    // PendingIntent request codes
    const val RC_FIRE = 10
    const val RC_SHOW = 11
    const val RC_DRANK = 12
    const val RC_SNOOZE = 13
    const val RC_DISMISS = 14
    const val RC_FULLSCREEN = 15

    const val EXTRA_AMOUNT = "extra_amount_ml"
    const val EXTRA_SKIP_COUNT = "extra_skip_count"
    const val EXTRA_LAST_DRINK = "extra_last_drink_millis"

    /** After this many consecutive "drink later" skips, the call screen shows a nudge. */
    const val SKIP_WARNING_THRESHOLD = 5
}
