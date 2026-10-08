package dev.sathish.learningdashboard.domain

import kotlin.math.roundToInt

/**
 * Single place where course progress is derived from lesson completion,
 * so the dashboard and the detail screen can never disagree.
 */
object ProgressCalculator {

    fun percent(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        val safeCompleted = completed.coerceIn(0, total)
        return (safeCompleted * 100.0 / total).roundToInt()
    }
}
