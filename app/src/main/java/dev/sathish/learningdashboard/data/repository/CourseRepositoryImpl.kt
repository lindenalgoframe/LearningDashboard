package dev.sathish.learningdashboard.data.repository

import dev.sathish.learningdashboard.data.local.CourseDao
import dev.sathish.learningdashboard.data.remote.CourseApi
import dev.sathish.learningdashboard.data.remote.safeApiCall
import dev.sathish.learningdashboard.data.toDomain
import dev.sathish.learningdashboard.data.toEntity
import dev.sathish.learningdashboard.domain.DataResult
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.domain.model.CourseDetail
import dev.sathish.learningdashboard.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first repository. Room is the single source of truth; the network only ever writes
 * into Room, and the UI only ever reads from Room. Room and Retrofit suspend functions are
 * main-safe, so no manual dispatcher switching is needed here.
 */
@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val api: CourseApi,
    private val dao: CourseDao,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses()
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override fun observeCourseDetail(courseId: Long): Flow<CourseDetail?> =
        combine(dao.observeCourse(courseId), dao.observeLessons(courseId)) { course, lessons ->
            course?.let { CourseDetail(it.toDomain(), lessons.map { lesson -> lesson.toDomain() }) }
        }.distinctUntilChanged()

    override suspend fun refreshCourses(): DataResult<Unit> = safeApiCall {
        val courses = api.getCourses()
        dao.replaceCourses(courses.mapIndexed { index, dto -> dto.toEntity(position = index) })
    }

    override suspend fun refreshCourseDetail(courseId: Long): DataResult<Unit> = safeApiCall {
        val detail = api.getCourseDetail(courseId)
        dao.mergeLessons(
            courseId = courseId,
            remote = detail.lessons.mapIndexed { index, dto -> dto.toEntity(courseId, position = index) },
        )
    }

    /**
     * Optimistic local write, flagged `pending_sync`. With a real backend, a WorkManager job
     * (network constraint, exponential backoff) would push pending rows and clear the flag.
     */
    override suspend fun markLessonCompleted(lessonId: Long) {
        dao.markLessonCompleted(lessonId)
    }

    override suspend fun clearCache() {
        dao.deleteAllCourses() // lessons are removed by ON DELETE CASCADE
    }
}
