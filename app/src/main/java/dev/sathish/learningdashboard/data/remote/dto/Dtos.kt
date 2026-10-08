package dev.sathish.learningdashboard.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)

/** Matches the `/courses` contract from the assignment. */
@Serializable
data class CourseDto(
    val id: Long,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int,
)

@Serializable
data class CourseDetailDto(
    val id: Long,
    val lessons: List<LessonDto>,
)

@Serializable
data class LessonDto(
    val id: Long,
    val title: String,
    val completed: Boolean,
)
