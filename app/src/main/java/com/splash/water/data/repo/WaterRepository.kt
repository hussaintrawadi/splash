package com.splash.water.data.repo

import com.splash.water.data.local.DayTotal
import com.splash.water.data.local.WaterLogDao
import com.splash.water.data.local.WaterLogEntity
import com.splash.water.domain.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** A single intake event in domain terms. */
data class WaterLog(
    val id: Long,
    val amountMl: Int,
    val timestamp: Long,
)

@Singleton
class WaterRepository @Inject constructor(
    private val dao: WaterLogDao,
) {
    suspend fun logWater(amountMl: Int, at: Long = DateUtils.nowMillis()): Long {
        return dao.insert(
            WaterLogEntity(amountMl = amountMl, timestamp = at, dayKey = DateUtils.dayKey(at))
        )
    }

    suspend fun deleteLog(id: Long) = dao.deleteById(id)

    fun observeTodayTotal(): Flow<Int> = dao.observeTotalForDay(DateUtils.todayKey())

    fun observeTotalForDay(date: LocalDate): Flow<Int> =
        dao.observeTotalForDay(DateUtils.dayKey(date))

    fun observeTodayLogs(): Flow<List<WaterLog>> = mapLogs(dao.observeForDay(DateUtils.todayKey()))

    fun observeLogsForDay(date: LocalDate): Flow<List<WaterLog>> =
        mapLogs(dao.observeForDay(DateUtils.dayKey(date)))

    /** Daily totals over an inclusive date range, keyed by "yyyy-MM-dd". */
    fun observeDailyTotals(start: LocalDate, end: LocalDate): Flow<List<DayTotal>> =
        dao.observeDailyTotals(DateUtils.dayKey(start), DateUtils.dayKey(end))

    fun observeAllDailyTotals(): Flow<List<DayTotal>> = dao.observeAllDailyTotals()

    suspend fun totalForDay(date: LocalDate): Int = dao.totalForDay(DateUtils.dayKey(date))

    suspend fun totalToday(): Int = dao.totalForDay(DateUtils.todayKey())

    /** Epoch millis of the most recent intake, or null if nothing logged yet. */
    suspend fun lastLogTime(): Long? = dao.lastLogTime()

    private fun mapLogs(flow: Flow<List<WaterLogEntity>>): Flow<List<WaterLog>> =
        flow.map { list -> list.map { WaterLog(it.id, it.amountMl, it.timestamp) } }
}
