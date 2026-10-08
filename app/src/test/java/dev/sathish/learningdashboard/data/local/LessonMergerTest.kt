package dev.sathish.learningdashboard.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards against a refresh silently undoing lessons the user completed while offline. */
class LessonMergerTest {

    private fun lesson(id: Long, completed: Boolean, pending: Boolean = false) =
        LessonEntity(id = id, courseId = 1, title = "Lesson $id", position = id.toInt(), isCompleted = completed, pendingSync = pending)

    @Test
    fun `local completion pending sync survives a refresh that has not seen it yet`() {
        val merged = LessonMerger.merge(
            remote = listOf(lesson(1, completed = false)),
            local = listOf(lesson(1, completed = true, pending = true)),
        ).single()

        assertTrue(merged.isCompleted)
        assertTrue(merged.pendingSync)
    }

    @Test
    fun `server confirmation clears the pending flag`() {
        val merged = LessonMerger.merge(
            remote = listOf(lesson(1, completed = true)),
            local = listOf(lesson(1, completed = true, pending = true)),
        ).single()

        assertTrue(merged.isCompleted)
        assertFalse(merged.pendingSync)
    }

    @Test
    fun `server wins when there are no local pending changes`() {
        val merged = LessonMerger.merge(
            remote = listOf(lesson(1, completed = false)),
            local = listOf(lesson(1, completed = true, pending = false)),
        ).single()

        assertFalse(merged.isCompleted)
    }

    @Test
    fun `lessons removed on the server are dropped`() {
        val merged = LessonMerger.merge(
            remote = listOf(lesson(1, completed = false)),
            local = listOf(lesson(1, completed = false), lesson(2, completed = true, pending = true)),
        )

        assertEquals(listOf(1L), merged.map { it.id })
    }
}
