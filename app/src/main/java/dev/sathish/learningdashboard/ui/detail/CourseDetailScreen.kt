package dev.sathish.learningdashboard.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
import dev.sathish.learningdashboard.ui.common.ProgressRing
import dev.sathish.learningdashboard.ui.common.StaleDataBanner
import dev.sathish.learningdashboard.ui.common.messageRes
import dev.sathish.learningdashboard.ui.theme.LearningDashboardTheme
import dev.sathish.learningdashboard.ui.theme.courseAccent

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
                    icon = Icons.Filled.Warning,
                    title = stringResource(R.string.error_title),
                    message = stringResource(state.error.messageRes()),
                    actionLabel = stringResource(R.string.retry),
                    onAction = onRetry,
                )

                is CourseDetailUiState.Content -> {
                    val accent = courseAccent(state.detail.course.id)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.staleReason?.let { reason ->
                            item(key = "stale_banner") { StaleDataBanner(reason) }
                        }
                        item(key = "header") { CourseHeader(state.detail, accent) }
                        item(key = "section") {
                            Row(
                                modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.lessons_header),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = stringResource(
                                        R.string.stat_fraction,
                                        state.detail.completedCount,
                                        state.detail.lessons.size,
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        itemsIndexed(state.detail.lessons, key = { _, lesson -> lesson.id }) { index, lesson ->
                            LessonRow(
                                number = index + 1,
                                lesson = lesson,
                                accent = accent,
                                onMarkCompleted = { onMarkCompleted(lesson.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseHeader(detail: CourseDetail, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(accent, lerp(accent, Color.Black, 0.3f))))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.course_label),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = detail.course.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.course_instructor, detail.course.instructor),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.lessons_completed, detail.completedCount, detail.lessons.size),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
        Spacer(Modifier.width(16.dp))
        ProgressRing(
            percent = detail.course.progressPercent,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f),
        )
    }
}

@Composable
private fun LessonRow(number: Int, lesson: Lesson, accent: Color, onMarkCompleted: () -> Unit) {
    val badgeColor by animateColorAsState(
        targetValue = if (lesson.isCompleted) accent else Color.Transparent,
        label = "badge",
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .then(
                        if (lesson.isCompleted) {
                            Modifier
                        } else {
                            Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline), CircleShape)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (lesson.isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(if (lesson.isCompleted) R.string.lesson_completed else R.string.lesson_pending),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (lesson.isCompleted) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!lesson.isCompleted) {
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onMarkCompleted,
                    border = BorderStroke(1.dp, accent),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.lesson_mark_complete),
                        color = accent,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
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
