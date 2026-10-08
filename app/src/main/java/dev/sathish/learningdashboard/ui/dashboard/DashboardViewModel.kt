package dev.sathish.learningdashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sathish.learningdashboard.domain.DataError
import dev.sathish.learningdashboard.domain.DataResult
import dev.sathish.learningdashboard.domain.ProgressCalculator
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.domain.repository.AuthRepository
import dev.sathish.learningdashboard.domain.repository.CourseRepository
import dev.sathish.learningdashboard.ui.common.RefreshStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(val error: DataError) : DashboardUiState
    data class Success(
        val courses: List<Course>,
        val isRefreshing: Boolean,
        /** Non-null when showing cached data because the latest refresh failed. */
        val staleReason: DataError?,
    ) : DashboardUiState {
        val totalLessons: Int get() = courses.sumOf { it.lessonCount }
        val completedLessons: Int
            get() = courses.sumOf { (it.lessonCount * it.progressPercent / 100.0).roundToInt() }
        val overallProgress: Int get() = ProgressCalculator.percent(completedLessons, totalLessons)
    }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.InProgress)
    private var refreshJob: Job? = null

    val uiState: StateFlow<DashboardUiState> =
        combine(courseRepository.observeCourses(), refreshStatus, this::reduce)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshStatus.value = RefreshStatus.InProgress
            refreshStatus.value = when (val result = courseRepository.refreshCourses()) {
                is DataResult.Success -> RefreshStatus.Idle
                is DataResult.Failure -> RefreshStatus.Failed(result.error)
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    /** Cached data always wins over an error: offline users still see their courses. */
    private fun reduce(courses: List<Course>, status: RefreshStatus): DashboardUiState = when {
        courses.isNotEmpty() -> DashboardUiState.Success(
            courses = courses,
            isRefreshing = status is RefreshStatus.InProgress,
            staleReason = (status as? RefreshStatus.Failed)?.error,
        )
        status is RefreshStatus.InProgress -> DashboardUiState.Loading
        status is RefreshStatus.Failed -> DashboardUiState.Error(status.error)
        else -> DashboardUiState.Empty
    }
}
