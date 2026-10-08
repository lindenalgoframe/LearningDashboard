package dev.sathish.learningdashboard.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {

    @Query(
        """
        SELECT c.*,
               COUNT(l.id) AS cached_lesson_count,
               COALESCE(SUM(l.is_completed), 0) AS completed_lesson_count
        FROM courses c
        LEFT JOIN lessons l ON l.course_id = c.id
        GROUP BY c.id
        ORDER BY c.position
        """,
    )
    abstract fun observeCourses(): Flow<List<CourseWithLessonStats>>

    @Query(
        """
        SELECT c.*,
               COUNT(l.id) AS cached_lesson_count,
               COALESCE(SUM(l.is_completed), 0) AS completed_lesson_count
        FROM courses c
        LEFT JOIN lessons l ON l.course_id = c.id
        WHERE c.id = :courseId
        GROUP BY c.id
        """,
    )
    abstract fun observeCourse(courseId: Long): Flow<CourseWithLessonStats?>

    @Query("SELECT * FROM lessons WHERE course_id = :courseId ORDER BY position")
    abstract fun observeLessons(courseId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE course_id = :courseId")
    abstract suspend fun getLessons(courseId: Long): List<LessonEntity>

    @Query("UPDATE lessons SET is_completed = 1, pending_sync = 1 WHERE id = :lessonId AND is_completed = 0")
    abstract suspend fun markLessonCompleted(lessonId: Long): Int

    @Query("DELETE FROM courses")
    abstract suspend fun deleteAllCourses()

    // @Upsert (UPDATE on conflict) rather than REPLACE: REPLACE deletes the row first, which would
    // cascade-delete the course's cached lessons and lose local completions.
    @Upsert
    abstract suspend fun upsertCourses(courses: List<CourseEntity>)

    @Upsert
    abstract suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM courses WHERE id NOT IN (:ids)")
    abstract suspend fun deleteCoursesNotIn(ids: List<Long>)

    @Query("DELETE FROM lessons WHERE course_id = :courseId AND id NOT IN (:ids)")
    abstract suspend fun deleteLessonsNotIn(courseId: Long, ids: List<Long>)

    @Query("DELETE FROM lessons WHERE course_id = :courseId")
    abstract suspend fun deleteLessonsForCourse(courseId: Long)

    /** Makes the local course list mirror the server's, atomically. */
    @Transaction
    open suspend fun replaceCourses(courses: List<CourseEntity>) {
        if (courses.isEmpty()) {
            deleteAllCourses()
        } else {
            deleteCoursesNotIn(courses.map { it.id })
            upsertCourses(courses)
        }
    }

    /** Read-merge-write in one transaction so a concurrent "mark complete" can't be lost. */
    @Transaction
    open suspend fun mergeLessons(courseId: Long, remote: List<LessonEntity>) {
        val merged = LessonMerger.merge(remote = remote, local = getLessons(courseId))
        if (merged.isEmpty()) {
            deleteLessonsForCourse(courseId)
        } else {
            deleteLessonsNotIn(courseId, merged.map { it.id })
            upsertLessons(merged)
        }
    }
}
