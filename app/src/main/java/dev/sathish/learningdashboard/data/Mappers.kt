package dev.sathish.learningdashboard.data

import dev.sathish.learningdashboard.data.local.CourseEntity
import dev.sathish.learningdashboard.data.local.CourseWithLessonStats
import dev.sathish.learningdashboard.data.local.LessonEntity
import dev.sathish.learningdashboard.data.remote.dto.CourseDto
import dev.sathish.learningdashboard.data.remote.dto.LessonDto
import dev.sathish.learningdashboard.domain.ProgressCalculator
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.domain.model.Lesson

fun CourseDto.toEntity(position: Int) = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
    serverProgress = progress,
    lessonCount = lessons,
    position = position,
)

fun LessonDto.toEntity(courseId: Long, position: Int) = LessonEntity(
    id = id,
    courseId = courseId,
    title = title,
    position = position,
    isCompleted = completed,
)

/**
 * Once a course's lessons are cached, progress is derived from them, so marking a lesson
 * complete is reflected immediately (and offline) on every screen. Before that, the
 * server-reported summary is shown.
 */
fun CourseWithLessonStats.toDomain(): Course {
    val hasLessons = cachedLessonCount > 0
    return Course(
        id = course.id,
        title = course.title,
        instructor = course.instructor,
        progressPercent = if (hasLessons) {
            ProgressCalculator.percent(completedLessonCount, cachedLessonCount)
        } else {
            course.serverProgress.coerceIn(0, 100)
        },
        lessonCount = if (hasLessons) cachedLessonCount else course.lessonCount,
    )
}

fun LessonEntity.toDomain() = Lesson(id = id, title = title, isCompleted = isCompleted)
