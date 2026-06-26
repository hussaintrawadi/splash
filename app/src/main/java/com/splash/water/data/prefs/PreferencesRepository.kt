package com.splash.water.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.splash.water.domain.model.ActivityLevel
import com.splash.water.domain.model.Climate
import com.splash.water.domain.model.ReminderMode
import com.splash.water.domain.model.Sex
import com.splash.water.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val ds = context.dataStore

    val preferences: Flow<UserPreferences> = ds.data.map { it.toUserPreferences() }

    suspend fun setOnboardingComplete(value: Boolean) = edit { it[Keys.ONBOARDING] = value }

    suspend fun updateProfile(
        weightKg: Double?,
        sex: Sex,
        age: Int?,
        activity: ActivityLevel,
        climate: Climate,
    ) = ds.edit {
        if (weightKg == null) it.remove(Keys.WEIGHT) else it[Keys.WEIGHT] = weightKg
        if (age == null) it.remove(Keys.AGE) else it[Keys.AGE] = age
        it[Keys.SEX] = sex.name
        it[Keys.ACTIVITY] = activity.name
        it[Keys.CLIMATE] = climate.name
    }

    /** Switch back to auto goal computed from body stats. */
    suspend fun useAutoGoal() = edit { it[Keys.GOAL_AUTO] = true }

    /** Set a manual goal (caller should pass a clamped value). */
    suspend fun setManualGoal(ml: Int) = ds.edit {
        it[Keys.GOAL_AUTO] = false
        it[Keys.MANUAL_GOAL] = ml
    }

    suspend fun setWakeWindow(startMinute: Int, endMinute: Int) = ds.edit {
        it[Keys.WAKE_START] = startMinute
        it[Keys.WAKE_END] = endMinute
    }

    suspend fun setReminderMode(mode: ReminderMode) = edit { it[Keys.REMINDER_MODE] = mode.name }
    suspend fun setIntervalMinutes(minutes: Int) = edit { it[Keys.INTERVAL] = minutes }
    suspend fun setSnoozeMinutes(minutes: Int) = edit { it[Keys.SNOOZE] = minutes }
    suspend fun setDefaultLogMl(ml: Int) = edit { it[Keys.DEFAULT_LOG] = ml }
    suspend fun setRemindersEnabled(value: Boolean) = edit { it[Keys.REMINDERS_ON] = value }
    suspend fun setSoundEnabled(value: Boolean) = edit { it[Keys.SOUND_ON] = value }
    suspend fun setVibrate(value: Boolean) = edit { it[Keys.VIBRATE] = value }
    suspend fun setSoundUri(uri: String?) = ds.edit {
        if (uri == null) it.remove(Keys.SOUND_URI) else it[Keys.SOUND_URI] = uri
    }
    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }

    /** Count one more "drink later" skip. */
    suspend fun incrementSkipCount() = ds.edit { it[Keys.SKIP_COUNT] = (it[Keys.SKIP_COUNT] ?: 0) + 1 }

    /** Reset the skip streak (called whenever water is actually logged). */
    suspend fun resetSkipCount() = edit { it[Keys.SKIP_COUNT] = 0 }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        ds.edit(block)
    }

    private fun Preferences.toUserPreferences() = UserPreferences(
        onboardingComplete = this[Keys.ONBOARDING] ?: false,
        weightKg = this[Keys.WEIGHT],
        sex = this[Keys.SEX]?.let { runCatching { Sex.valueOf(it) }.getOrNull() } ?: Sex.UNSPECIFIED,
        age = this[Keys.AGE],
        activity = this[Keys.ACTIVITY]?.let { runCatching { ActivityLevel.valueOf(it) }.getOrNull() }
            ?: ActivityLevel.LIGHT,
        climate = this[Keys.CLIMATE]?.let { runCatching { Climate.valueOf(it) }.getOrNull() }
            ?: Climate.TEMPERATE,
        goalIsAuto = this[Keys.GOAL_AUTO] ?: true,
        manualGoalMl = this[Keys.MANUAL_GOAL] ?: com.splash.water.domain.GoalCalculator.DEFAULT_GOAL_ML,
        wakeStartMinute = this[Keys.WAKE_START] ?: (8 * 60),
        wakeEndMinute = this[Keys.WAKE_END] ?: (22 * 60),
        reminderMode = this[Keys.REMINDER_MODE]?.let { runCatching { ReminderMode.valueOf(it) }.getOrNull() }
            ?: ReminderMode.SMART,
        intervalMinutes = this[Keys.INTERVAL] ?: 120,
        snoozeMinutes = this[Keys.SNOOZE] ?: 15,
        defaultLogMl = this[Keys.DEFAULT_LOG] ?: 250,
        remindersEnabled = this[Keys.REMINDERS_ON] ?: true,
        soundEnabled = this[Keys.SOUND_ON] ?: true,
        vibrate = this[Keys.VIBRATE] ?: true,
        soundUri = this[Keys.SOUND_URI],
        themeMode = this[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM,
        skipCount = this[Keys.SKIP_COUNT] ?: 0,
    )

    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val WEIGHT = doublePreferencesKey("weight_kg")
        val SEX = stringPreferencesKey("sex")
        val AGE = intPreferencesKey("age")
        val ACTIVITY = stringPreferencesKey("activity")
        val CLIMATE = stringPreferencesKey("climate")
        val GOAL_AUTO = booleanPreferencesKey("goal_is_auto")
        val MANUAL_GOAL = intPreferencesKey("manual_goal_ml")
        val WAKE_START = intPreferencesKey("wake_start_minute")
        val WAKE_END = intPreferencesKey("wake_end_minute")
        val REMINDER_MODE = stringPreferencesKey("reminder_mode")
        val INTERVAL = intPreferencesKey("interval_minutes")
        val SNOOZE = intPreferencesKey("snooze_minutes")
        val DEFAULT_LOG = intPreferencesKey("default_log_ml")
        val REMINDERS_ON = booleanPreferencesKey("reminders_enabled")
        val SOUND_ON = booleanPreferencesKey("sound_enabled")
        val VIBRATE = booleanPreferencesKey("vibrate")
        val SOUND_URI = stringPreferencesKey("sound_uri")
        val THEME = stringPreferencesKey("theme_mode")
        val SKIP_COUNT = intPreferencesKey("skip_count")
    }
}
