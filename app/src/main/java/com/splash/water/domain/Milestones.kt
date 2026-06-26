package com.splash.water.domain

enum class MilestoneType { DAILY_ML, DAILY_GOAL, WEEKLY_DAYS, STREAK }

/** Icon identity for a milestone card. Mapped to a Material vector icon in the UI layer. */
enum class MilestoneIcon { DROP, WAVE, SPARKLE, TROPHY, CALENDAR, FIRE, CROWN, TARGET, BOLT, MEDAL, STAR, SHIELD, DIAMOND, PREMIUM }

/**
 * Static definition of a milestone card. Tips are light, friendly motivation, not medical claims.
 * [color] is an ARGB long used to tint the reward card.
 */
data class MilestoneDef(
    val id: String,
    val title: String,
    val tip: String,
    val icon: MilestoneIcon,
    val type: MilestoneType,
    val threshold: Int = 0, // ml for DAILY_ML, day-count for WEEKLY_DAYS / STREAK
    val color: Long,
)

object MilestoneDefs {
    // Daily rewards are no longer a fixed list, they're goal-relative and rotate daily.
    // See [DailyRewards] and [DailyStep].

    val weekly: List<MilestoneDef> = listOf(
        MilestoneDef("week_3", "Getting Consistent", "3 goal days this week, habits are forming!", MilestoneIcon.CALENDAR, MilestoneType.WEEKLY_DAYS, 3, 0xFF7E57C2),
        MilestoneDef("week_5", "Hydration Habit", "5 goal days this week. You're on fire!", MilestoneIcon.FIRE, MilestoneType.WEEKLY_DAYS, 5, 0xFFFF7043),
        MilestoneDef("week_7", "Perfect Week", "7/7 days, a flawless hydration week!", MilestoneIcon.CROWN, MilestoneType.WEEKLY_DAYS, 7, 0xFFFFCA28),
    )

    val streak: List<MilestoneDef> = listOf(
        MilestoneDef("streak_3", "On a Roll", "3-day streak! Consistency is everything.", MilestoneIcon.TARGET, MilestoneType.STREAK, 3, 0xFF26A69A),
        MilestoneDef("streak_7", "Week Warrior", "7-day streak, a full week of hitting your goal!", MilestoneIcon.BOLT, MilestoneType.STREAK, 7, 0xFFEF5350),
        MilestoneDef("streak_30", "Monthly Master", "30-day streak, a whole month of dedication!", MilestoneIcon.MEDAL, MilestoneType.STREAK, 30, 0xFFAB47BC),
        MilestoneDef("streak_90", "Quarterly Champion", "3 months straight. Outstanding commitment!", MilestoneIcon.STAR, MilestoneType.STREAK, 90, 0xFF42A5F5),
        MilestoneDef("streak_180", "Half-Year Hero", "6 months of daily hydration. Incredible!", MilestoneIcon.SHIELD, MilestoneType.STREAK, 180, 0xFF26C6DA),
        MilestoneDef("streak_240", "Eight-Month Elite", "8 months strong, you live hydrated now.", MilestoneIcon.DIAMOND, MilestoneType.STREAK, 240, 0xFF7E57C2),
        MilestoneDef("streak_365", "One-Year Legend", "365 days! A full year of hitting your goal.", MilestoneIcon.CROWN, MilestoneType.STREAK, 365, 0xFFFFC107),
        MilestoneDef("streak_545", "18-Month Master", "18 months of unbroken hydration. Legendary.", MilestoneIcon.PREMIUM, MilestoneType.STREAK, 545, 0xFFFF7043),
        MilestoneDef("streak_730", "Two-Year Titan", "2 years straight. You're a true hydration titan!", MilestoneIcon.TROPHY, MilestoneType.STREAK, 730, 0xFFEC407A),
    )

    /** Persisted, gallery milestones (weekly + long-term). Daily steps are computed live. */
    val all: List<MilestoneDef> = weekly + streak

    fun byId(id: String): MilestoneDef? = all.firstOrNull { it.id == id }
}
