package com.splash.water.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splash.water.data.local.DayTotal
import com.splash.water.data.prefs.PreferencesRepository
import com.splash.water.data.prefs.UserPreferences
import com.splash.water.data.repo.WaterLog
import com.splash.water.data.repo.WaterRepository
import com.splash.water.domain.DateUtils
import com.splash.water.ui.components.BarPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

enum class HistoryTab(val label: String) { DAY("Day"), WEEK("Week"), MONTH("Month"), YEAR("Year") }

data class StatItem(val label: String, val value: String)

data class HistoryUiState(
    val tab: HistoryTab = HistoryTab.WEEK,
    val bars: List<BarPoint> = emptyList(),
    val goalMl: Int = 3000,
    val stats: List<StatItem> = emptyList(),
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val waterRepo: WaterRepository,
    private val prefsRepo: PreferencesRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(HistoryTab.WEEK)

    fun selectTab(tab: HistoryTab) {
        selectedTab.value = tab
    }

    val uiState: StateFlow<HistoryUiState> = selectedTab.flatMapLatest { tab ->
        val today = DateUtils.today()
        when (tab) {
            HistoryTab.DAY -> combine(prefsRepo.preferences, waterRepo.observeTodayLogs()) { prefs, logs ->
                buildDay(prefs, logs)
            }
            HistoryTab.WEEK -> combine(
                prefsRepo.preferences,
                waterRepo.observeDailyTotals(today.minusDays(6), today),
            ) { prefs, totals -> buildWeek(prefs, totals, today) }
            HistoryTab.MONTH -> combine(
                prefsRepo.preferences,
                waterRepo.observeDailyTotals(DateUtils.startOfMonth(today), today),
            ) { prefs, totals -> buildMonth(prefs, totals, today) }
            HistoryTab.YEAR -> combine(
                prefsRepo.preferences,
                waterRepo.observeDailyTotals(DateUtils.startOfYear(today), today),
            ) { prefs, totals -> buildYear(prefs, totals, today) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    private fun buildDay(prefs: UserPreferences, logs: List<WaterLog>): HistoryUiState {
        val buckets = IntArray(8) // eight 3-hour buckets
        logs.forEach { log ->
            val hour = Instant.ofEpochMilli(log.timestamp).atZone(DateUtils.zone()).hour
            buckets[hour / 3] += log.amountMl
        }
        val nowBucket = LocalTime.now(DateUtils.zone()).hour / 3
        val bars = buckets.mapIndexed { i, v -> BarPoint("${i * 3}", v, highlight = i == nowBucket) }
        val total = logs.sumOf { it.amountMl }
        return HistoryUiState(
            tab = HistoryTab.DAY,
            bars = bars,
            goalMl = prefs.goalMl,
            stats = listOf(
                StatItem("Total", "$total ml"),
                StatItem("Drinks", "${logs.size}"),
                StatItem("Goal", "${prefs.goalMl} ml"),
                StatItem("Left", "${(prefs.goalMl - total).coerceAtLeast(0)} ml"),
            ),
            loading = false,
        )
    }

    private fun buildWeek(prefs: UserPreferences, totals: List<DayTotal>, today: LocalDate): HistoryUiState {
        val map = totals.associate { it.dayKey to it.total }
        val days = (0..6).map { today.minusDays((6 - it).toLong()) }
        val bars = days.map {
            BarPoint(DateUtils.shortDayName(it), map[DateUtils.dayKey(it)] ?: 0, highlight = it == today)
        }
        val total = bars.sumOf { it.value }
        val metDays = days.count { (map[DateUtils.dayKey(it)] ?: 0) >= prefs.goalMl }
        return HistoryUiState(
            tab = HistoryTab.WEEK,
            bars = bars,
            goalMl = prefs.goalMl,
            stats = listOf(
                StatItem("Total", litres(total)),
                StatItem("Daily avg", "${total / 7} ml"),
                StatItem("Goal days", "$metDays/7"),
                StatItem("Best", "${bars.maxOf { it.value }} ml"),
            ),
            loading = false,
        )
    }

    private fun buildMonth(prefs: UserPreferences, totals: List<DayTotal>, today: LocalDate): HistoryUiState {
        val map = totals.associate { it.dayKey to it.total }
        val start = DateUtils.startOfMonth(today)
        val days = (0..(today.dayOfMonth - 1)).map { start.plusDays(it.toLong()) }
        val bars = days.map {
            val d = it.dayOfMonth
            BarPoint(
                label = if (d == 1 || d % 5 == 0) "$d" else "",
                value = map[DateUtils.dayKey(it)] ?: 0,
                highlight = it == today,
            )
        }
        val total = bars.sumOf { it.value }
        val elapsed = days.size.coerceAtLeast(1)
        val metDays = days.count { (map[DateUtils.dayKey(it)] ?: 0) >= prefs.goalMl }
        return HistoryUiState(
            tab = HistoryTab.MONTH,
            bars = bars,
            goalMl = prefs.goalMl,
            stats = listOf(
                StatItem("Total", litres(total)),
                StatItem("Daily avg", "${total / elapsed} ml"),
                StatItem("Goal days", "$metDays/$elapsed"),
                StatItem("Best", "${bars.maxOf { it.value }} ml"),
            ),
            loading = false,
        )
    }

    private fun buildYear(prefs: UserPreferences, totals: List<DayTotal>, today: LocalDate): HistoryUiState {
        val byMonth = IntArray(12)
        totals.forEach { dt ->
            val month = dt.dayKey.substring(5, 7).toInt() // "yyyy-MM-dd"
            byMonth[month - 1] += dt.total
        }
        val bars = (1..today.monthValue).map { m ->
            BarPoint(DateUtils.shortMonthName(m), byMonth[m - 1], highlight = m == today.monthValue)
        }
        val total = bars.sumOf { it.value }
        val months = today.monthValue.coerceAtLeast(1)
        return HistoryUiState(
            tab = HistoryTab.YEAR,
            bars = bars,
            goalMl = prefs.goalMl,
            stats = listOf(
                StatItem("Total", litres(total)),
                StatItem("Monthly avg", litres(total / months)),
                StatItem("Best", litres(bars.maxOf { it.value })),
            ),
            loading = false,
        )
    }

    private fun litres(ml: Int): String = "%.1f L".format(ml / 1000.0)
}
