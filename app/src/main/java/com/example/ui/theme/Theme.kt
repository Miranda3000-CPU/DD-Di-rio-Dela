package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PeriodRosePrimary,
    onPrimary = Color.White,
    primaryContainer = PeriodRoseContainer,
    onPrimaryContainer = Color(0xFF4A0013),
    secondary = FertileBluePrimary,
    onSecondary = Color.White,
    secondaryContainer = FertileBlueContainer,
    onSecondaryContainer = Color(0xFF00274B),
    tertiary = OvulationPurplePrimary,
    onTertiary = Color.White,
    tertiaryContainer = OvulationPurpleContainer,
    onTertiaryContainer = Color(0xFF2A1052),
    background = SoftBackground,
    onBackground = TextPrimary,
    surface = SoftSurface,
    onSurface = TextPrimary,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outlineVariant = SoftBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = PeriodRoseSecondary,
    onPrimary = Color(0xFF5C000B),
    primaryContainer = Color(0xFF8C1D24),
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = FertileBlueSecondary,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = OvulationPurplePrimary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4A148C),
    onTertiaryContainer = Color(0xFFEDE7F6),
    background = Color(0xFF1C1B1E),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF25232A),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

@Composable
fun MeuCicloTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
