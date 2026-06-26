package com.splash.water.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.repo.WaterLog
import com.splash.water.data.repo.WaterRepository
import com.splash.water.domain.Streaks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val currentMl: Int = 0,
    val goalMl: Int = 3000,
    val logs: List<WaterLog> = emptyList(),
    val streak: Int = 0,
    val defaultLogMl: Int = 250,
) {
    val progress: Float get() = if (goalMl <= 0) 0f else (currentMl.toFloat() / goalMl).coerceIn(0f, 1f)
    val remainingMl: Int get() = (goalMl - currentMl).coerceAtLeast(0)
    val goalReached: Boolean get() = currentMl >= goalMl && goalMl > 0
}

sealed interface HomeEvent {
    data class Logged(val amountMl: Int) : HomeEvent
    data object GoalReached : HomeEvent
    data class MilestoneUnlocked(val def: com.splash.water.domain.MilestoneDef) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val waterRepo: WaterRepository,
    private val prefsRepo: PreferencesRepository,
    private val milestones: com.splash.water.domain.MilestoneEngine,
    private val reminderScheduler: com.splash.water.reminder.ReminderScheduler,
) : ViewModel() {

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    init {
        // Ensure the next reminder is scheduled whenever the app is opened.
        viewModelScope.launch { reminderScheduler.reschedule() }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        prefsRepo.preferences,
        waterRepo.observeTodayTotal(),
        waterRepo.observeTodayLogs(),
        waterRepo.observeAllDailyTotals(),
    ) { prefs, total, logs, dailyTotals ->
        val goal = prefs.goalMl
        val streak = Streaks.currentStreak(dailyTotals.associate { it.dayKey to it.total }, goal)
        HomeUiState(
            loading = false,
            currentMl = total,
            goalMl = goal,
            logs = logs,
            streak = streak,
            defaultLogMl = prefs.defaultLogMl,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun log(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            val goal = uiState.value.goalMl
            val before = waterRepo.totalToday()
            waterRepo.logWater(amountMl)
            prefsRepo.resetSkipCount()
            val after = before + amountMl
            _events.tryEmit(HomeEvent.Logged(amountMl))
            if (before < goal && after >= goal) _events.tryEmit(HomeEvent.GoalReached)
            // Unlock any daily milestones crossed by this log and reveal their cards.
            milestones.evaluateDaily(after, goal).forEach {
                _events.tryEmit(HomeEvent.MilestoneUnlocked(it))
            }
            // Logging changes how much is left, so recompute the smart schedule.
            reminderScheduler.reschedule()
        }
    }

    fun undo(log: WaterLog) {
        viewModelScope.launch { waterRepo.deleteLog(log.id) }
    }
}
