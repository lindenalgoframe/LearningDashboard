package dev.sathish.learningdashboard.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sathish.learningdashboard.R
import dev.sathish.learningdashboard.domain.DataError
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.ui.common.MessageView
import dev.sathish.learningdashboard.ui.common.StaleDataBanner
import dev.sathish.learningdashboard.ui.common.messageRes
import dev.sathish.learningdashboard.ui.theme.LearningDashboardTheme

@Composable
fun DashboardScreen(
    onCourseClick: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(
        state = state,
        onCourseClick = onCourseClick,
        onRefresh = viewModel::refresh,
        onLogout = viewModel::logout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onCourseClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = { TextButton(onClick = onLogout) { Text(stringResource(R.string.logout)) } },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                DashboardUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                DashboardUiState.Empty -> MessageView(
                    title = stringResource(R.string.empty_title),
                    message = stringResource(R.string.empty_message),
                    actionLabel = stringResource(R.string.refresh),
                    onAction = onRefresh,
                )

                is DashboardUiState.Error -> MessageView(
                    title = stringResource(R.string.error_title),
                    message = stringResource(state.error.messageRes()),
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRefresh,
                )

                is DashboardUiState.Success -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        state.staleReason?.let { reason ->
                            item(key = "stale_banner") { StaleDataBanner(reason) }
                        }
                        items(state.courses, key = { it.id }) { course ->
                            CourseCard(course = course, onClick = { onCourseClick(course.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.course_instructor, course.instructor),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { course.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.course_progress, course.progressPercent),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        pluralStringResource(R.plurals.lesson_count, course.lessonCount, course.lessonCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(onClick = onClick) { Text(stringResource(R.string.course_continue)) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    LearningDashboardTheme {
        DashboardContent(
            state = DashboardUiState.Success(
                courses = listOf(
                    Course(1, "Python Programming", "John Smith", 65, 20),
                    Course(2, "Generative AI", "Sarah Williams", 40, 16),
                ),
                isRefreshing = false,
                staleReason = DataError.NETWORK,
            ),
            onCourseClick = {},
            onRefresh = {},
            onLogout = {},
        )
    }
}
