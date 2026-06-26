package com.splash.water.data.repo

import com.splash.water.data.local.ReminderSlotDao
import com.splash.water.data.local.ReminderSlotEntity
import com.splash.water.domain.model.ReminderKind
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSlotRepository @Inject constructor(
    private val dao: ReminderSlotDao,
) {
    fun observeAll(): Flow<List<ReminderSlotEntity>> = dao.observeAll()

    suspend fun enabledSlots(): List<ReminderSlotEntity> = dao.enabledSlots()

    suspend fun addFixed(minuteOfDay: Int): Long =
        dao.insert(ReminderSlotEntity(kind = ReminderKind.FIXED.name, minuteOfDay = minuteOfDay))

    suspend fun addInterval(intervalMinutes: Int): Long =
        dao.insert(ReminderSlotEntity(kind = ReminderKind.INTERVAL.name, intervalMinutes = intervalMinutes))

    suspend fun setEnabled(slot: ReminderSlotEntity, enabled: Boolean) =
        dao.update(slot.copy(enabled = enabled))

    suspend fun delete(slot: ReminderSlotEntity) = dao.delete(slot)
}
