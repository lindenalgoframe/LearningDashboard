package dev.sathish.learningdashboard.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sathish.learningdashboard.domain.DataError
import dev.sathish.learningdashboard.domain.DataResult
import dev.sathish.learningdashboard.domain.model.CourseDetail
import dev.sathish.learningdashboard.domain.repository.CourseRepository
import dev.sathish.learningdashboard.ui.common.RefreshStatus
import dev.sathish.learningdashboard.ui.navigation.CourseDetailDestination
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CourseDetailUiState {
    data object Loading : CourseDetailUiState
    data class Error(val error: DataError) : CourseDetailUiState
    data class Content(
        val detail: CourseDetail,
        val isRefreshing: Boolean,
        val staleReason: DataError?,
    ) : CourseDetailUiState
}

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val courseId: Long = savedStateHandle.toRoute<CourseDetailDestination>().courseId

    private val refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.InProgress)
    private var refreshJob: Job? = null

    val uiState: StateFlow<CourseDetailUiState> =
        combine(courseRepository.observeCourseDetail(courseId), refreshStatus, this::reduce)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshStatus.value = RefreshStatus.InProgress
            refreshStatus.value = when (val result = courseRepository.refreshCourseDetail(courseId)) {
                is DataResult.Success -> RefreshStatus.Idle
                is DataResult.Failure -> RefreshStatus.Failed(result.error)
            }
        }
    }

    /**
     * Writes locally; Room re-emits, so both this screen's progress and the dashboard update
     * immediately, online or offline.
     */
    fun markLessonCompleted(lessonId: Long) {
        viewModelScope.launch { courseRepository.markLessonCompleted(lessonId) }
    }

    private fun reduce(detail: CourseDetail?, status: RefreshStatus): CourseDetailUiState = when {
        detail != null && detail.lessons.isNotEmpty() -> CourseDetailUiState.Content(
            detail = detail,
            isRefreshing = status is RefreshStatus.InProgress,
            staleReason = (status as? RefreshStatus.Failed)?.error,
        )
        status is RefreshStatus.InProgress -> CourseDetailUiState.Loading
        status is RefreshStatus.Failed -> CourseDetailUiState.Error(status.error)
        // Refresh succeeded but nothing is cached: the course no longer exists.
        else -> CourseDetailUiState.Error(DataError.NOT_FOUND)
    }
}
