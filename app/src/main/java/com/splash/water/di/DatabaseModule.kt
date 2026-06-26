package com.splash.water.di

import android.content.Context
import androidx.room.Room
import com.splash.water.data.local.AchievementDao
import com.splash.water.data.local.ReminderSlotDao
import com.splash.water.data.local.SplashDatabase
import com.splash.water.data.local.WaterLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SplashDatabase =
        Room.databaseBuilder(context, SplashDatabase::class.java, SplashDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWaterLogDao(db: SplashDatabase): WaterLogDao = db.waterLogDao()

    @Provides
    fun provideAchievementDao(db: SplashDatabase): AchievementDao = db.achievementDao()

    @Provides
    fun provideReminderSlotDao(db: SplashDatabase): ReminderSlotDao = db.reminderSlotDao()
}
