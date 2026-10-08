package dev.sathish.learningdashboard.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val instructor: String,
    /** Progress as reported by the server; used until lessons are cached locally. */
    @ColumnInfo(name = "server_progress") val serverProgress: Int,
    @ColumnInfo(name = "lesson_count") val lessonCount: Int,
    /** Preserves server ordering. */
    val position: Int,
)

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("course_id")],
)
data class LessonEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "course_id") val courseId: Long,
    val title: String,
    val position: Int,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    /** True when completed locally and not yet confirmed by the server (outbox marker). */
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = false,
)

/** A course row plus lesson aggregates computed in SQL, so progress updates reactively. */
data class CourseWithLessonStats(
    @Embedded val course: CourseEntity,
    @ColumnInfo(name = "cached_lesson_count") val cachedLessonCount: Int,
    @ColumnInfo(name = "completed_lesson_count") val completedLessonCount: Int,
)
