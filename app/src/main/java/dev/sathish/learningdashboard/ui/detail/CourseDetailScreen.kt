package dev.sathish.learningdashboard.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sathish.learningdashboard.R
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.domain.model.CourseDetail
import dev.sathish.learningdashboard.domain.model.Lesson
import dev.sathish.learningdashboard.ui.common.MessageView
import dev.sathish.learningdashboard.ui.common.StaleDataBanner
import dev.sathish.learningdashboard.ui.common.messageRes
import dev.sathish.learningdashboard.ui.theme.LearningDashboardTheme

@Composable
fun CourseDetailScreen(
    onBack: () -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CourseDetailContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onMarkCompleted = viewModel::markLessonCompleted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailContent(
    state: CourseDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMarkCompleted: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (state as? CourseDetailUiState.Content)?.detail?.course?.title.orEmpty()
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state) {
                CourseDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                is CourseDetailUiState.Error -> MessageView(
                    title = stringResource(R.string.error_title),
                    message = stringResource(state.error.messageRes()),
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRetry,
                )

                is CourseDetailUiState.Content -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.staleReason?.let { reason ->
                        item(key = "stale_banner") { StaleDataBanner(reason) }
                    }
                    item(key = "header") { ProgressHeader(state.detail) }
                    items(state.detail.lessons, key = { it.id }) { lesson ->
                        LessonRow(lesson = lesson, onMarkCompleted = { onMarkCompleted(lesson.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(detail: CourseDetail) {
    Column(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(detail.course.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.course_progress, detail.course.progressPercent),
            style = MaterialTheme.typography.titleMedium,
        )
        LinearProgressIndicator(
            progress = { detail.course.progressPercent / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(R.string.lessons_completed, detail.completedCount, detail.lessons.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onMarkCompleted: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(lesson.title) },
            supportingContent = {
                if (lesson.isCompleted) {
                    Text(stringResource(R.string.lesson_completed), color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(stringResource(R.string.lesson_pending))
                }
            },
            trailingContent = {
                if (lesson.isCompleted) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                } else {
                    TextButton(onClick = onMarkCompleted) { Text(stringResource(R.string.lesson_mark_complete)) }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreview() {
    LearningDashboardTheme {
        CourseDetailContent(
            state = CourseDetailUiState.Content(
                detail = CourseDetail(
                    course = Course(1, "Python Programming", "John Smith", 50, 4),
                    lessons = listOf(
                        Lesson(1, "Introduction", true),
                        Lesson(2, "Variables & Data Types", true),
                        Lesson(3, "Functions", false),
                        Lesson(4, "OOP", false),
                    ),
                ),
                isRefreshing = false,
                staleReason = null,
            ),
            onBack = {},
            onRetry = {},
            onMarkCompleted = {},
        )
    }
}
