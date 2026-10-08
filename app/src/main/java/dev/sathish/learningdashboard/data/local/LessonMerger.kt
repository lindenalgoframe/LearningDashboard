package dev.sathish.learningdashboard.data.local

/**
 * Merges freshly fetched lessons with what is stored locally.
 *
 * Rule: the server is the source of truth, except for completions the user made locally that
 * the server hasn't acknowledged yet (`pendingSync`). Those must survive a refresh, otherwise a
 * pull-to-refresh would silently undo the user's work.
 * Lessons missing from the server response are dropped (only `remote` items are returned).
 */
object LessonMerger {

    fun merge(remote: List<LessonEntity>, local: List<LessonEntity>): List<LessonEntity> {
        val localById = local.associateBy { it.id }
        return remote.map { remoteLesson ->
            val localLesson = localById[remoteLesson.id]
            if (localLesson != null && localLesson.pendingSync && !remoteLesson.isCompleted) {
                remoteLesson.copy(isCompleted = true, pendingSync = true)
            } else {
                remoteLesson.copy(pendingSync = false)
            }
        }
    }
}
