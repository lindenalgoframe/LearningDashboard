package dev.sathish.learningdashboard.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sathish.learningdashboard.MainViewModel
import dev.sathish.learningdashboard.ui.dashboard.DashboardScreen
import dev.sathish.learningdashboard.ui.detail.CourseDetailScreen
import dev.sathish.learningdashboard.ui.login.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination

@Serializable
data object DashboardDestination

@Serializable
data class CourseDetailDestination(val courseId: Long)

/**
 * Auth state drives navigation: logging in, logging out (or a future token expiry) all flip
 * the session, and this host moves between the login and authenticated graphs.
 */
@Composable
fun AppNavHost(mainViewModel: MainViewModel = hiltViewModel()) {
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val loggedIn = isLoggedIn
    if (loggedIn == null) {
        // Session is read from disk in a few ms; render nothing rather than flashing Login.
        Box(Modifier.fillMaxSize())
        return
    }

    val navController = rememberNavController()
    val startDestination: Any = remember { if (loggedIn) DashboardDestination else LoginDestination }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginDestination> {
            LoginScreen()
        }
        composable<DashboardDestination> {
            DashboardScreen(onCourseClick = { id -> navController.navigate(CourseDetailDestination(id)) })
        }
        composable<CourseDetailDestination> {
            CourseDetailScreen(onBack = { navController.popBackStack() })
        }
    }

    LaunchedEffect(loggedIn) {
        val onLogin = navController.currentDestination?.hasRoute<LoginDestination>() == true
        when {
            loggedIn && onLogin -> navController.resetTo(DashboardDestination)
            !loggedIn && !onLogin -> navController.resetTo(LoginDestination)
        }
    }
}

private fun NavHostController.resetTo(destination: Any) {
    navigate(destination) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
