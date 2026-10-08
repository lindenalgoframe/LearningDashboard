package dev.sathish.learningdashboard.domain.repository

import dev.sathish.learningdashboard.domain.DataResult
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.domain.model.CourseDetail
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    suspend fun login(email: String, password: String): DataResult<Unit>
    suspend fun logout()
}

/**
 * Offline-first: `observe*` streams always come from the local database (single source of truth);
 * `refresh*` fetches from the network and writes into the database.
 */
interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>
    fun observeCourseDetail(courseId: Long): Flow<CourseDetail?>
    suspend fun refreshCourses(): DataResult<Unit>
    suspend fun refreshCourseDetail(courseId: Long): DataResult<Unit>
    suspend fun markLessonCompleted(lessonId: Long)
    suspend fun clearCache()
}
