package dev.sathish.learningdashboard.ui.theme

import androidx.compose.ui.graphics.Color

private val Accents = listOf(
    Color(0xFF6366F1), // indigo
    Color(0xFFEC4899), // pink
    Color(0xFF0EA5E9), // sky
    Color(0xFF14B8A6), // teal
    Color(0xFF8B5CF6), // violet
    Color(0xFFF97316), // orange
)

/** Stable accent per course, so a course keeps its colour across the dashboard and detail screen. */
fun courseAccent(courseId: Long): Color =
    Accents[(courseId - 1).mod(Accents.size.toLong()).toInt()]
