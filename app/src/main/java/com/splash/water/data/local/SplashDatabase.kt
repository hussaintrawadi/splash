package com.splash.water.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WaterLogEntity::class,
        AchievementEntity::class,
        ReminderSlotEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SplashDatabase : RoomDatabase() {
    abstract fun waterLogDao(): WaterLogDao
    abstract fun achievementDao(): AchievementDao
    abstract fun reminderSlotDao(): ReminderSlotDao

    companion object {
        const val NAME = "splash.db"
    }
}
