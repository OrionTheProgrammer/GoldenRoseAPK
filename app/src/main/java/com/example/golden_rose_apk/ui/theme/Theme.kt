package com.example.golden_rose_apk.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema oscuro optimizado para contraste y legibilidad.
 */
private val DarkColorScheme = darkColorScheme(
    primary = ValorantRed,
    secondary = GoldenAccent,
    tertiary = GoldenAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = TextPrimary,
    onSecondary = Color(0xFF1B1F24),
    onTertiary = Color(0xFF1B1F24),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF394452)
)

/**
 * Esquema claro con acentos inspirados en Valorant.
 */
private val LightColorScheme = lightColorScheme(
    primary = ValorantRed,
    secondary = Color(0xFF6D5DD3),
    tertiary = GoldenAccent,
    background = Color(0xFFF5F6FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7ECF3),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF2D2A32),
    onBackground = Color(0xFF0F1923),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCDD5DF)
)

/**
 * Tema principal de la aplicación.
 */
@Composable
fun GoldenRoseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
