package com.splash.water.domain

/**
 * Daily rewards are based on a **percentage of your personal goal** (not fixed litres), so anyone
 * can unlock every step no matter their goal. The flavour (titles + tips + colour) rotates each
 * day from a themed pool, and higher levels unlock more themes, so the cards keep feeling fresh
 * instead of repeating the same four every single day.
 */
enum class DailyStep(val percent: Int, val baseTitle: String, val icon: MilestoneIcon) {
    Q1(25, "Off to a Start", MilestoneIcon.DROP),
    Q2(50, "Halfway Hero", MilestoneIcon.WAVE),
    Q3(75, "Almost There", MilestoneIcon.BOLT),
    GOAL(100, "Goal Crushed", MilestoneIcon.TROPHY),
    BONUS(125, "Overachiever", MilestoneIcon.STAR);

    fun thresholdMl(goalMl: Int): Int = (goalMl * percent) / 100

    /** A standalone card definition used for the home-screen celebration when this step is crossed. */
    fun toMilestoneDef(): MilestoneDef = MilestoneDef(
        id = "daily_${name.lowercase()}",
        title = baseTitle,
        tip = "",
        icon = icon,
        type = MilestoneType.DAILY_GOAL,
        threshold = percent,
        color = when (this) {
            Q1 -> 0xFF4FC3F7
            Q2 -> 0xFF29B6F6
            Q3 -> 0xFF7E57C2
            GOAL -> 0xFF66BB6A
            BONUS -> 0xFFFFC107
        },
    )
}

/** One day's worth of flavour for the five steps. */
data class DailyTheme(
    val id: String,
    val name: String,
    val minLevel: Int,
    val accent: Long,
    val titles: Map<DailyStep, String>,
    val tips: Map<DailyStep, String>,
)

object DailyRewards {
    private fun theme(
        id: String,
        name: String,
        minLevel: Int,
        accent: Long,
        s: List<Pair<String, String>>,
    ) = DailyTheme(
        id = id,
        name = name,
        minLevel = minLevel,
        accent = accent,
        titles = DailyStep.entries.mapIndexed { i, step -> step to s[i].first }.toMap(),
        tips = DailyStep.entries.mapIndexed { i, step -> step to s[i].second }.toMap(),
    )

    val themes: List<DailyTheme> = listOf(
        theme("glow", "Glow", 1, 0xFFFFB74D, listOf(
            "First Drops" to "Your glow-up starts with a single sip.",
            "Morning Dew" to "Halfway to radiant, keep it flowing.",
            "Fresh Face" to "Almost there. Your skin says thanks.",
            "Glowing Skin" to "Goal hit! Hydration fuels a healthy glow.",
            "Radiant Overflow" to "Above and beyond. Pure radiance.",
        )),
        theme("focus", "Focus", 1, 0xFF42A5F5, listOf(
            "Warm Up" to "A sip to wake the mind.",
            "In the Zone" to "Halfway, focus sharpening.",
            "Locked In" to "Clear head, steady flow.",
            "Peak Focus" to "Goal hit! Hydrated minds think clearer.",
            "Limitless" to "Beyond the goal. Unstoppable.",
        )),
        theme("energy", "Energy", 2, 0xFFFF7043, listOf(
            "Spark" to "First sip, first spark.",
            "Charging" to "Halfway charged up.",
            "Powered Up" to "Energy climbing fast.",
            "Full Power" to "Goal hit! Fully charged.",
            "Supercharged" to "Overflowing with energy.",
        )),
        theme("calm", "Calm", 2, 0xFF26C6DA, listOf(
            "First Ripple" to "A calm, easy start.",
            "Steady Flow" to "Halfway, nice and easy.",
            "Smooth Sailing" to "Almost there, stay calm.",
            "Inner Calm" to "Goal hit! Balanced and hydrated.",
            "Zen Master" to "Beyond calm. Beyond goal.",
        )),
        theme("athlete", "Athlete", 3, 0xFF66BB6A, listOf(
            "Warm-Up Lap" to "Hydration is rep number one.",
            "Halftime" to "Halfway, refuel and go.",
            "Final Stretch" to "Push it to the finish.",
            "Champion" to "Goal hit! Peak-performance fuel.",
            "Record Breaker" to "Past the line. New personal best.",
        )),
        theme("adventure", "Adventure", 3, 0xFF7E57C2, listOf(
            "Base Camp" to "Every climb starts right here.",
            "Halfway Up" to "Halfway to the summit.",
            "Almost Summit" to "The peak is within reach.",
            "Summit Reached" to "Goal hit! You conquered it.",
            "New Horizon" to "Past the peak. Onward.",
        )),
        theme("bloom", "Bloom", 4, 0xFFFF6B9D, listOf(
            "First Sprout" to "Water makes everything grow.",
            "Budding" to "Halfway to full bloom.",
            "Blossoming" to "Almost in full bloom.",
            "Full Bloom" to "Goal hit! Absolutely flourishing.",
            "Garden of Eden" to "Overflowing with life.",
        )),
        theme("cosmic", "Cosmic", 5, 0xFF5C6BC0, listOf(
            "Liftoff" to "Fuel the launch with a sip.",
            "Orbit" to "Halfway to the stars.",
            "Deep Space" to "Cruising past the milestones.",
            "Touchdown" to "Goal hit! You're a star.",
            "Supernova" to "Beyond the goal. Brilliant.",
        )),
    )

    /** Themes available at [level] (always at least the first one). */
    fun unlockedThemes(level: Int): List<DailyTheme> =
        themes.filter { it.minLevel <= level }.ifEmpty { listOf(themes.first()) }

    /**
     * The theme shown on [epochDay] for someone at [level]. Deterministic per day (stable for the
     * whole day) but rotates day to day and pulls from a bigger pool as you level up.
     */
    fun themeForDay(level: Int, epochDay: Long): DailyTheme {
        val eligible = unlockedThemes(level)
        val idx = ((epochDay * 7 + 3).mod(eligible.size.toLong())).toInt()
        return eligible[idx]
    }
}
