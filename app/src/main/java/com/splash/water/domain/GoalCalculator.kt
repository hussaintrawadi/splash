package com.splash.water.domain

import com.splash.water.domain.model.ActivityLevel
import com.splash.water.domain.model.Season
import com.splash.water.domain.model.Sex

/**
 * Computes a daily *drinking water* goal (fluids you actively drink) from body stats.
 *
 * Evidence base:
 *  - U.S. National Academies of Sciences, Engineering, and Medicine (2004) Adequate Intake for
 *    total water: ~3.7 L/day (men) and ~2.7 L/day (women). About 20% of total water comes from
 *    food, leaving a "from beverages" target of roughly 3.0 L (men) / 2.2 L (women).
 *  - EFSA (2010) Adequate Intake for total water: 2.5 L/day (men), 2.0 L/day (women).
 *  - Weight-based clinical estimate widely used for adults: ~30-35 mL/kg/day, with older adults
 *    nearer 25-30 mL/kg/day.
 *  - Exercise & heat raise needs (ACSM/NATA): added via [ActivityLevel] and [Season].
 *
 * Result is clamped to [MIN_GOAL_ML]..[MAX_GOAL_ML]. These are friendly estimates for motivation,
 * not medical advice; people with medical conditions should follow their clinician's guidance.
 */
object GoalCalculator {
    const val DEFAULT_GOAL_ML = 3000
    const val MIN_GOAL_ML = 1500
    const val MAX_GOAL_ML = 4000

    /** Auto goal from stats; falls back to a sex-appropriate Adequate Intake when weight is unknown. */
    fun computeGoal(
        weightKg: Double?,
        sex: Sex,
        age: Int?,
        activity: ActivityLevel,
        season: Season,
    ): Int {
        if (weightKg == null || weightKg <= 0.0) return defaultForSex(sex)

        // mL per kg, adjusted for age (younger need a touch more, older slightly less).
        val mlPerKg = when {
            age == null -> 33.0
            age < 30 -> 35.0
            age <= 55 -> 33.0
            else -> 30.0
        }
        var base = weightKg * mlPerKg
        // Women carry a lower fraction of body water (~50% vs ~60%), so scale slightly.
        if (sex == Sex.FEMALE) base *= 0.95

        val raw = base + activity.extraMl + season.extraMl
        return clamp(roundTo50(raw))
    }

    /** AI "from beverages" target when we only know sex. */
    private fun defaultForSex(sex: Sex): Int = when (sex) {
        Sex.MALE -> 3000
        Sex.FEMALE -> 2300
        Sex.UNSPECIFIED -> DEFAULT_GOAL_ML
    }

    /** Clamp any goal (including manual edits) into the safe range. */
    fun clamp(ml: Int): Int = ml.coerceIn(MIN_GOAL_ML, MAX_GOAL_ML)

    /** True when a manual goal is close to the safe edges, so the UI can show a gentle warning. */
    fun isNearLimit(ml: Int): Boolean = ml <= MIN_GOAL_ML + 200 || ml >= MAX_GOAL_ML - 200

    /** Short, user-facing explanation of the method (shown in Settings). */
    const val EXPLANATION: String =
        "Your goal starts from ~30-35 mL per kg of body weight (the common clinical guideline, " +
            "adjusted for age and sex), then adds for activity and the season. It's kept within " +
            "1.5-4 L/day, in line with the National Academies and EFSA adequate-intake ranges. " +
            "These are general estimates, not medical advice."

    private fun roundTo50(ml: Double): Int = (Math.round(ml / 50.0) * 50).toInt()
}
