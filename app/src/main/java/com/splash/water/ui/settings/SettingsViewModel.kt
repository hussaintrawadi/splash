package com.splash.water.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splash.water.data.local.ReminderSlotEntity
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.prefs.UserPreferences
import com.splash.water.data.repo.ReminderSlotRepository
import com.splash.water.domain.GoalCalculator
import com.splash.water.domain.model.ActivityLevel
import com.splash.water.domain.model.Climate
import com.splash.water.domain.model.ReminderMode
import com.splash.water.domain.model.Sex
import com.splash.water.domain.model.ThemeMode
import com.splash.water.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: PreferencesRepository,
    private val slotRepo: ReminderSlotRepository,
    private val scheduler: ReminderScheduler,
) : ViewModel() {

    val prefs: StateFlow<UserPreferences?> =
        prefsRepo.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val slots: StateFlow<List<ReminderSlotEntity>> =
        slotRepo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun reschedule() = viewModelScope.launch { scheduler.reschedule() }

    fun saveProfile(weightKg: Double?, sex: Sex, age: Int?, activity: ActivityLevel, climate: Climate) {
        viewModelScope.launch {
            prefsRepo.updateProfile(weightKg, sex, age, activity, climate)
            scheduler.reschedule()
        }
    }

    fun useAutoGoal() = viewModelScope.launch { prefsRepo.useAutoGoal() }
    fun setManualGoal(ml: Int) = viewModelScope.launch { prefsRepo.setManualGoal(GoalCalculator.clamp(ml)) }

    fun setReminderMode(mode: ReminderMode) = viewModelScope.launch {
        prefsRepo.setReminderMode(mode); scheduler.reschedule()
    }

    fun setWakeWindow(startMinute: Int, endMinute: Int) = viewModelScope.launch {
        prefsRepo.setWakeWindow(startMinute, endMinute); scheduler.reschedule()
    }

    fun setIntervalMinutes(min: Int) = viewModelScope.launch {
        prefsRepo.setIntervalMinutes(min); scheduler.reschedule()
    }

    fun setSnoozeMinutes(min: Int) = viewModelScope.launch { prefsRepo.setSnoozeMinutes(min) }
    fun setDefaultLog(ml: Int) = viewModelScope.launch { prefsRepo.setDefaultLogMl(ml) }

    fun setRemindersEnabled(value: Boolean) = viewModelScope.launch {
        prefsRepo.setRemindersEnabled(value); scheduler.reschedule()
    }

    fun setSoundEnabled(value: Boolean) = viewModelScope.launch { prefsRepo.setSoundEnabled(value) }
    fun setVibrate(value: Boolean) = viewModelScope.launch { prefsRepo.setVibrate(value) }
    fun setSoundUri(uri: String?) = viewModelScope.launch { prefsRepo.setSoundUri(uri) }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefsRepo.setThemeMode(mode) }

    fun addFixedSlot(minuteOfDay: Int) = viewModelScope.launch {
        slotRepo.addFixed(minuteOfDay); scheduler.reschedule()
    }

    fun addIntervalSlot(minutes: Int) = viewModelScope.launch {
        slotRepo.addInterval(minutes); scheduler.reschedule()
    }

    fun toggleSlot(slot: ReminderSlotEntity, enabled: Boolean) = viewModelScope.launch {
        slotRepo.setEnabled(slot, enabled); scheduler.reschedule()
    }

    fun deleteSlot(slot: ReminderSlotEntity) = viewModelScope.launch {
        slotRepo.delete(slot); scheduler.reschedule()
    }
}
