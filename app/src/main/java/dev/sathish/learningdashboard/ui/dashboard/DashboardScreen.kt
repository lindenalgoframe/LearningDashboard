package dev.sathish.learningdashboard.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sathish.learningdashboard.R
import dev.sathish.learningdashboard.domain.DataError
import dev.sathish.learningdashboard.domain.model.Course
import dev.sathish.learningdashboard.ui.common.AccentProgressBar
import dev.sathish.learningdashboard.ui.common.CourseAvatar
import dev.sathish.learningdashboard.ui.common.MessageView
import dev.sathish.learningdashboard.ui.common.SkeletonBlock
import dev.sathish.learningdashboard.ui.common.StaleDataBanner
import dev.sathish.learningdashboard.ui.common.messageRes
import dev.sathish.learningdashboard.ui.common.rememberSkeletonPulse
import dev.sathish.learningdashboard.ui.theme.HeroGradient
import dev.sathish.learningdashboard.ui.theme.LearningDashboardTheme
import dev.sathish.learningdashboard.ui.theme.courseAccent
import java.util.Calendar

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(greetingRes()),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(stringResource(R.string.dashboard_title), style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = stringResource(R.string.logout))
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
                DashboardUiState.Loading -> DashboardSkeleton()

                DashboardUiState.Empty -> MessageView(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = stringResource(R.string.empty_title),
                    message = stringResource(R.string.empty_message),
                    actionLabel = stringResource(R.string.refresh),
                    onAction = onRefresh,
                )

                is DashboardUiState.Error -> MessageView(
                    icon = Icons.Filled.Warning,
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
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        state.staleReason?.let { reason ->
                            item(key = "stale_banner") { StaleDataBanner(reason) }
                        }
                        item(key = "summary") { ProgressSummaryCard(state) }
                        item(key = "section") {
                            Text(
                                text = stringResource(R.string.section_your_courses),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                            )
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

@Composable
private fun ProgressSummaryCard(state: DashboardUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(HeroGradient))
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.overall_progress),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.percent_value, state.overallProgress),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
        )
        Spacer(Modifier.height(12.dp))
        AccentProgressBar(
            percent = state.overallProgress,
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f),
        )
        Spacer(Modifier.height(16.dp))
        Row {
            SummaryStat(
                value = state.courses.size.toString(),
                label = pluralStringResource(R.plurals.stat_courses, state.courses.size),
                modifier = Modifier.weight(1f),
            )
            SummaryStat(
                value = stringResource(R.string.stat_fraction, state.completedLessons, state.totalLessons),
                label = stringResource(R.string.stat_lessons_done),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    val accent = courseAccent(course.id)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CourseAvatar(title = course.title, accent = accent)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.course_byline,
                            course.instructor,
                            pluralStringResource(R.plurals.lesson_count, course.lessonCount, course.lessonCount),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.percent_value, course.progressPercent),
                    style = MaterialTheme.typography.titleMedium,
                    color = accent,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.complete_suffix),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            AccentProgressBar(percent = course.progressPercent, color = accent)

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(stringResource(R.string.course_continue))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Placeholder shaped like the real content, so the layout doesn't jump when data arrives. */
@Composable
private fun DashboardSkeleton() {
    val pulse = rememberSkeletonPulse()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBlock(
            Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(MaterialTheme.shapes.extraLarge),
            pulse,
        )
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkeletonBlock(Modifier.size(48.dp), pulse)
                        Spacer(Modifier.width(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonBlock(Modifier.width(180.dp).height(16.dp), pulse)
                            SkeletonBlock(Modifier.width(120.dp).height(12.dp), pulse)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    SkeletonBlock(Modifier.fillMaxWidth().height(8.dp), pulse)
                }
            }
        }
    }
}

private fun greetingRes(): Int = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> R.string.greeting_morning
    in 12..16 -> R.string.greeting_afternoon
    else -> R.string.greeting_evening
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
                    Course(3, "Full Stack Development", "David Brown", 25, 28),
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

@Preview(showBackground = true)
@Composable
private fun DashboardLoadingPreview() {
    LearningDashboardTheme {
        DashboardContent(state = DashboardUiState.Loading, onCourseClick = {}, onRefresh = {}, onLogout = {})
    }
}
