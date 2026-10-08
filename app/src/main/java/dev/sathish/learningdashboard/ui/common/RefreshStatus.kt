package dev.sathish.learningdashboard.ui.common

import dev.sathish.learningdashboard.domain.DataError

/** Status of the latest network refresh, combined with cached data to derive screen state. */
sealed interface RefreshStatus {
    data object InProgress : RefreshStatus
    data object Idle : RefreshStatus
    data class Failed(val error: DataError) : RefreshStatus
}
