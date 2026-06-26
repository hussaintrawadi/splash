package com.splash.water.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Insert
    suspend fun insert(log: WaterLogEntity): Long

    @Delete
    suspend fun delete(log: WaterLogEntity)

    @Query("DELETE FROM water_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM water_log WHERE dayKey = :dayKey ORDER BY timestamp DESC")
    fun observeForDay(dayKey: String): Flow<List<WaterLogEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_log WHERE dayKey = :dayKey")
    fun observeTotalForDay(dayKey: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_log WHERE dayKey = :dayKey")
    suspend fun totalForDay(dayKey: String): Int

    @Query(
        "SELECT dayKey AS dayKey, COALESCE(SUM(amountMl), 0) AS total FROM water_log " +
            "WHERE dayKey BETWEEN :startKey AND :endKey GROUP BY dayKey ORDER BY dayKey ASC"
    )
    fun observeDailyTotals(startKey: String, endKey: String): Flow<List<DayTotal>>

    @Query(
        "SELECT dayKey AS dayKey, COALESCE(SUM(amountMl), 0) AS total FROM water_log " +
            "GROUP BY dayKey ORDER BY dayKey ASC"
    )
    fun observeAllDailyTotals(): Flow<List<DayTotal>>

    @Query("SELECT * FROM water_log ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<WaterLogEntity>>

    @Query("SELECT MAX(timestamp) FROM water_log")
    suspend fun lastLogTime(): Long?
}

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(achievement: AchievementEntity): Long

    @Query("SELECT * FROM achievement ORDER BY unlockedAt DESC")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM achievement WHERE defId = :defId AND periodKey = :periodKey)")
    suspend fun exists(defId: String, periodKey: String): Boolean

    @Query("SELECT COUNT(*) FROM achievement WHERE defId = :defId")
    fun observeCount(defId: String): Flow<Int>
}

@Dao
interface ReminderSlotDao {
    @Insert
    suspend fun insert(slot: ReminderSlotEntity): Long

    @Update
    suspend fun update(slot: ReminderSlotEntity)

    @Delete
    suspend fun delete(slot: ReminderSlotEntity)

    @Query("SELECT * FROM reminder_slot ORDER BY kind ASC, minuteOfDay ASC")
    fun observeAll(): Flow<List<ReminderSlotEntity>>

    @Query("SELECT * FROM reminder_slot WHERE enabled = 1")
    suspend fun enabledSlots(): List<ReminderSlotEntity>
}
