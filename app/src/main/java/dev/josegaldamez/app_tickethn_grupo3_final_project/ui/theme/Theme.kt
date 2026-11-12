package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6B96FF),
    onPrimary = Color(0xFF07162B),
    primaryContainer = Color(0xFF182D58),
    onPrimaryContainer = Color(0xFFDCE8FF),
    secondaryContainer = Color(0xFF14382F),
    onSecondaryContainer = Color(0xFFAFF1D7),
    tertiaryContainer = Color(0xFF302855),
    onTertiaryContainer = Color(0xFFE9DEFF),
    background = Color(0xFF091429),
    surface = Color(0xFF11213B),
    onBackground = Color(0xFFF5F8FF),
    onSurface = Color(0xFFF5F8FF),
    surfaceVariant = Color(0xFF1E2F4D),
    onSurfaceVariant = Color(0xFFB5C3D8),
    outline = Color(0xFF7788A6),
    outlineVariant = Color(0xFF2B3B55)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFEAFF),
    onPrimaryContainer = Color(0xFF163B8A),
    secondaryContainer = Color(0xFFD8F6EB),
    onSecondaryContainer = Color(0xFF086649),
    tertiaryContainer = Color(0xFFECE7FD),
    onTertiaryContainer = Color(0xFF472F92),
    background = Color(0xFFF7F9FC),
    surface = Color.White,
    onBackground = Color(0xFF0B1F4D),
    onSurface = Color(0xFF0B1F4D),
    surfaceVariant = Color(0xFFEDF1F7),
    onSurfaceVariant = Color(0xFF52627A),
    outline = Color(0xFF687993),
    outlineVariant = Color(0xFFDCE4EF)
)

@Composable
fun Apptickethngrupo3finalprojectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
