package com.splash.water.domain

/**
 * Hydration levels. Your level is driven by how many days you've ever met your goal (your
 * personal goal, whatever it is), so it's fair regardless of whether your goal is 1.5L or 4L.
 * Levels gate which daily reward themes appear, so the rewards keep evolving as you progress.
 */
data class LevelDef(
    val level: Int,
    val title: String,
    val minGoalDays: Int,
    val icon: MilestoneIcon,
    val color: Long,
)

object Levels {
    val all: List<LevelDef> = listOf(
        LevelDef(1, "Droplet", 0, MilestoneIcon.DROP, 0xFF4FC3F7),
        LevelDef(2, "Sprinkle", 3, MilestoneIcon.WAVE, 0xFF29B6F6),
        LevelDef(3, "Stream", 7, MilestoneIcon.BOLT, 0xFF26A69A),
        LevelDef(4, "River", 14, MilestoneIcon.TARGET, 0xFF66BB6A),
        LevelDef(5, "Wave Rider", 30, MilestoneIcon.STAR, 0xFFFFB74D),
        LevelDef(6, "Tide Turner", 60, MilestoneIcon.MEDAL, 0xFFFF7043),
        LevelDef(7, "Hydro Hero", 100, MilestoneIcon.SHIELD, 0xFF7E57C2),
        LevelDef(8, "Aqua Master", 180, MilestoneIcon.DIAMOND, 0xFF42A5F5),
        LevelDef(9, "Ocean Sage", 365, MilestoneIcon.CROWN, 0xFFFFC107),
        LevelDef(10, "Hydration Legend", 730, MilestoneIcon.TROPHY, 0xFFEC407A),
    )

    /** The level for someone who has met their goal on [goalDays] total days. */
    fun levelFor(goalDays: Int): LevelDef = all.last { goalDays >= it.minGoalDays }

    /** The next level above [current], or null if already at the top. */
    fun next(current: LevelDef): LevelDef? = all.firstOrNull { it.level == current.level + 1 }

    /** Progress (0f..1f) from the start of [current] toward [next]. 1f at max level. */
    fun progress(goalDays: Int, current: LevelDef): Float {
        val next = next(current) ?: return 1f
        val span = (next.minGoalDays - current.minGoalDays).coerceAtLeast(1)
        return ((goalDays - current.minGoalDays).toFloat() / span).coerceIn(0f, 1f)
    }
}
