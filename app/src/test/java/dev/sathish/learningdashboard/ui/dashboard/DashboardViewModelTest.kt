package dev.sathish.learningdashboard.ui.dashboard

import dev.sathish.learningdashboard.domain.DataError
import dev.sathish.learningdashboard.domain.DataResult
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.domain.model.CourseDetail
import dev.sathish.learningdashboard.domain.repository.AuthRepository
import dev.sathish.learningdashboard.domain.repository.CourseRepository
import dev.sathish.learningdashboard.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The core promise of the dashboard: cached courses are shown even when the network fails,
 * and an error screen appears only when there is genuinely nothing to show.
 */
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCourseRepository()
    private val python = Course(1, "Python Programming", "John Smith", 65, 20)

    @Test
    fun `shows loading while first refresh is in flight and nothing is cached`() = runTest {
        repository.pendingRefresh = CompletableDeferred()
        val viewModel = createViewModel()

        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `offline with cached courses shows cached data flagged as stale`() = runTest {
        repository.courses.value = listOf(python)
        repository.refreshResult = DataResult.Failure(DataError.NETWORK)

        val viewModel = createViewModel()

        assertEquals(
            DashboardUiState.Success(courses = listOf(python), isRefreshing = false, staleReason = DataError.NETWORK),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `offline with empty cache shows error, and retry recovers`() = runTest {
        repository.refreshResult = DataResult.Failure(DataError.NETWORK)
        val viewModel = createViewModel()
        assertEquals(DashboardUiState.Error(DataError.NETWORK), viewModel.uiState.value)

        // Connectivity returns: the refresh writes into the cache, as the real repository does.
        repository.refreshResult = DataResult.Success(Unit)
        repository.onRefreshSuccess = { repository.courses.value = listOf(python) }
        viewModel.refresh()

        assertEquals(
            DashboardUiState.Success(courses = listOf(python), isRefreshing = false, staleReason = null),
            viewModel.uiState.value,
        )
        assertEquals(2, repository.refreshCalls)
    }

    @Test
    fun `successful refresh with no courses shows empty state`() = runTest {
        repository.refreshResult = DataResult.Success(Unit)

        val viewModel = createViewModel()

        assertEquals(DashboardUiState.Empty, viewModel.uiState.value)
    }

    /** Creates the ViewModel and keeps its WhileSubscribed state flow active. */
    private fun TestScope.createViewModel(): DashboardViewModel {
        val viewModel = DashboardViewModel(repository, FakeAuthRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }
}

private class FakeCourseRepository : CourseRepository {
    val courses = MutableStateFlow<List<Course>>(emptyList())
    var refreshResult: DataResult<Unit> = DataResult.Success(Unit)
    var pendingRefresh: CompletableDeferred<DataResult<Unit>>? = null
    var onRefreshSuccess: () -> Unit = {}
    var refreshCalls = 0

    override fun observeCourses(): Flow<List<Course>> = courses

    override suspend fun refreshCourses(): DataResult<Unit> {
        refreshCalls++
        val result = pendingRefresh?.await() ?: refreshResult
        if (result is DataResult.Success) onRefreshSuccess()
        return result
    }

    override fun observeCourseDetail(courseId: Long): Flow<CourseDetail?> = emptyFlow()
    override suspend fun refreshCourseDetail(courseId: Long): DataResult<Unit> = DataResult.Success(Unit)
    override suspend fun markLessonCompleted(lessonId: Long) = Unit
    override suspend fun clearCache() {
        courses.value = emptyList()
    }
}

private class FakeAuthRepository : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = MutableStateFlow(true)
    override suspend fun login(email: String, password: String): DataResult<Unit> = DataResult.Success(Unit)
    override suspend fun logout() = Unit
}
