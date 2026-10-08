package dev.sathish.learningdashboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Brand gradient used on hero surfaces (login header, dashboard summary). Same in light and dark. */
val HeroGradient = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = Color(0xFFF6F6FA),
    onBackground = Color(0xFF14141F),
    surface = Color.White,
    onSurface = Color(0xFF14141F),
    surfaceVariant = Color(0xFFEEEEF4),
    onSurfaceVariant = Color(0xFF5E5E72),
    outline = Color(0xFFC4C4D2),
    outlineVariant = Color(0xFFE6E6EE),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFAFD),
    surfaceContainer = Color(0xFFF3F3F8),
    surfaceContainerHigh = Color(0xFFEDEDF3),
    surfaceContainerHighest = Color(0xFFE7E7EE),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF422006),
    secondaryContainer = Color(0xFF422006),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = Color(0xFF0F0F17),
    onBackground = Color(0xFFE8E8F0),
    surface = Color(0xFF181824),
    onSurface = Color(0xFFE8E8F0),
    surfaceVariant = Color(0xFF232333),
    onSurfaceVariant = Color(0xFFA3A3B8),
    outline = Color(0xFF4A4A60),
    outlineVariant = Color(0xFF2B2B3C),
    surfaceContainerLowest = Color(0xFF0B0B12),
    surfaceContainerLow = Color(0xFF14141F),
    surfaceContainer = Color(0xFF1B1B28),
    surfaceContainerHigh = Color(0xFF232332),
    surfaceContainerHighest = Color(0xFF2B2B3C),
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Brand theme. Dynamic (wallpaper) colour is deliberately off so the product looks the same on
 * every device; course accents carry the visual variety instead.
 */
@Composable
fun LearningDashboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
