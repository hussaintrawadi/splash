package com.splash.water.data.repo

import com.splash.water.data.local.AchievementDao
import com.splash.water.data.local.AchievementEntity
import com.splash.water.domain.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val dao: AchievementDao,
) {
    fun observeAll(): Flow<List<AchievementEntity>> = dao.observeAll()

    fun observeCount(defId: String): Flow<Int> = dao.observeCount(defId)

    suspend fun isUnlocked(defId: String, periodKey: String): Boolean = dao.exists(defId, periodKey)

    /** Unlock a milestone for a period. Returns true if it was newly unlocked. */
    suspend fun unlock(defId: String, periodKey: String): Boolean {
        if (dao.exists(defId, periodKey)) return false
        val rowId = dao.insertIgnore(
            AchievementEntity(defId = defId, periodKey = periodKey, unlockedAt = DateUtils.nowMillis())
        )
        return rowId != -1L
    }
}
