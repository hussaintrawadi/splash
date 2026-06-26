package com.splash.water.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.repo.AchievementRepository
import com.splash.water.data.repo.WaterRepository
import com.splash.water.domain.DailyRewards
import com.splash.water.domain.DailyStep
import com.splash.water.domain.DateUtils
import com.splash.water.domain.LevelDef
import com.splash.water.domain.Levels
import com.splash.water.domain.MilestoneDef
import com.splash.water.domain.MilestoneDefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** A persisted gallery milestone (weekly / long-term). */
data class RewardCard(val def: MilestoneDef, val unlocked: Boolean, val count: Int)

/** A live daily card, unlock state reflects today's progress vs a % of the personal goal. */
data class DailyRewardCard(
    val step: DailyStep,
    val title: String,
    val tip: String,
    val accent: Long,
    val thresholdMl: Int,
    val unlocked: Boolean,
)

data class LevelInfo(
    val def: LevelDef,
    val next: LevelDef?,
    val goalDays: Int,
    val progress: Float,
    val daysToNext: Int,
)

data class RewardsUiState(
    val level: LevelInfo? = null,
    val themeName: String = "",
    val daily: List<DailyRewardCard> = emptyList(),
    val weekly: List<RewardCard> = emptyList(),
    val streak: List<RewardCard> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    achievementRepo: AchievementRepository,
    waterRepo: WaterRepository,
    prefsRepo: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<RewardsUiState> = combine(
        prefsRepo.preferences,
        waterRepo.observeTodayTotal(),
        waterRepo.observeAllDailyTotals(),
        achievementRepo.observeAll(),
    ) { prefs, todayMl, dailyTotals, achievements ->
        val goal = prefs.goalMl

        // Level from lifetime goal-met days (fair for any goal size).
        val goalDays = if (goal > 0) dailyTotals.count { it.total >= goal } else 0
        val levelDef = Levels.levelFor(goalDays)
        val nextLevel = Levels.next(levelDef)
        val level = LevelInfo(
            def = levelDef,
            next = nextLevel,
            goalDays = goalDays,
            progress = Levels.progress(goalDays, levelDef),
            daysToNext = nextLevel?.let { (it.minGoalDays - goalDays).coerceAtLeast(0) } ?: 0,
        )

        // Today's rotating theme decorates the five % steps; unlock state is live.
        val theme = DailyRewards.themeForDay(levelDef.level, DateUtils.today().toEpochDay())
        val daily = DailyStep.entries.map { step ->
            DailyRewardCard(
                step = step,
                title = theme.titles.getValue(step),
                tip = theme.tips.getValue(step),
                accent = theme.accent,
                thresholdMl = step.thresholdMl(goal),
                unlocked = goal > 0 && todayMl >= step.thresholdMl(goal),
            )
        }

        val counts = achievements.groupingBy { it.defId }.eachCount()
        fun cards(defs: List<MilestoneDef>) = defs.map { def ->
            val c = counts[def.id] ?: 0
            RewardCard(def, unlocked = c > 0, count = c)
        }
        val weekly = cards(MilestoneDefs.weekly)
        val streak = cards(MilestoneDefs.streak)

        val all = daily.count { it.unlocked } + weekly.count { it.unlocked } + streak.count { it.unlocked }
        RewardsUiState(
            level = level,
            themeName = theme.name,
            daily = daily,
            weekly = weekly,
            streak = streak,
            unlockedCount = all,
            totalCount = daily.size + weekly.size + streak.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RewardsUiState())
}
