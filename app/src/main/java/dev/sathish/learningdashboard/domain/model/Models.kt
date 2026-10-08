package dev.sathish.learningdashboard.domain.model

data class Course(
    val id: Long,
    val title: String,
    val instructor: String,
    val progressPercent: Int,
    val lessonCount: Int,
)

data class Lesson(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
)

data class CourseDetail(
    val course: Course,
    val lessons: List<Lesson>,
) {
    val completedCount: Int get() = lessons.count { it.isCompleted }
}
