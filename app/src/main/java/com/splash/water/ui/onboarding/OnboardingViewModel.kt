package com.splash.water.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.domain.model.ActivityLevel
import com.splash.water.domain.model.Season
import com.splash.water.domain.model.ReminderMode
import com.splash.water.domain.model.Sex
import com.splash.water.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefsRepo: PreferencesRepository,
    private val scheduler: ReminderScheduler,
) : ViewModel() {

    fun complete(
        useStats: Boolean,
        weightKg: Double?,
        sex: Sex,
        age: Int?,
        activity: ActivityLevel,
        season: Season,
        wakeStart: Int,
        wakeEnd: Int,
        mode: ReminderMode,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            if (useStats && weightKg != null) {
                prefsRepo.updateProfile(weightKg, sex, age, activity, season)
            }
            prefsRepo.useAutoGoal()
            prefsRepo.setWakeWindow(wakeStart, wakeEnd)
            prefsRepo.setReminderMode(mode)
            prefsRepo.setOnboardingComplete(true)
            scheduler.reschedule()
            onDone()
        }
    }
}
