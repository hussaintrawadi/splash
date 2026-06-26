package com.splash.water.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AppState(
    val loading: Boolean = true,
    val onboardingComplete: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    prefsRepo: PreferencesRepository,
) : ViewModel() {
    val state: StateFlow<AppState> = prefsRepo.preferences
        .map { AppState(loading = false, onboardingComplete = it.onboardingComplete, themeMode = it.themeMode) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppState(loading = true))
}
