package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = DarkAmeenColors.primary,
    onPrimary = DarkAmeenColors.onPrimary,
    primaryContainer = DarkAmeenColors.primaryContainer,
    onPrimaryContainer = DarkAmeenColors.onPrimaryContainer,
    secondary = DarkAmeenColors.secondary,
    onSecondary = DarkAmeenColors.onSecondary,
    background = DarkAmeenColors.background,
    surface = DarkAmeenColors.surface,
    surfaceVariant = DarkAmeenColors.surfaceVariant,
    surfaceContainer = DarkAmeenColors.surface,
    surfaceContainerHigh = DarkAmeenColors.surfaceVariant,
    surfaceContainerHighest = DarkAmeenColors.surfaceVariant,
    surfaceContainerLow = DarkAmeenColors.background,
    surfaceContainerLowest = DarkAmeenColors.background,
    onBackground = DarkAmeenColors.textPrimary,
    onSurface = DarkAmeenColors.textPrimary,
    onSurfaceVariant = DarkAmeenColors.textSecondary,
    outline = DarkAmeenColors.border,
    outlineVariant = DarkAmeenColors.cardBorder,
    error = DarkAmeenColors.criticalRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightAmeenColors.primary,
    onPrimary = LightAmeenColors.onPrimary,
    primaryContainer = LightAmeenColors.primaryContainer,
    onPrimaryContainer = LightAmeenColors.onPrimaryContainer,
    secondary = LightAmeenColors.secondary,
    onSecondary = LightAmeenColors.onSecondary,
    background = LightAmeenColors.background,
    surface = LightAmeenColors.surface,
    surfaceVariant = LightAmeenColors.surfaceVariant,
    surfaceContainer = LightAmeenColors.surface,
    surfaceContainerHigh = LightAmeenColors.surfaceVariant,
    surfaceContainerHighest = LightAmeenColors.surfaceVariant,
    surfaceContainerLow = LightAmeenColors.background,
    surfaceContainerLowest = LightAmeenColors.background,
    onBackground = LightAmeenColors.textPrimary,
    onSurface = LightAmeenColors.textPrimary,
    onSurfaceVariant = LightAmeenColors.textSecondary,
    outline = LightAmeenColors.border,
    outlineVariant = LightAmeenColors.cardBorder,
    error = LightAmeenColors.criticalRed,
    onError = Color.White
)

@Composable
fun AmeenTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val ameenColors = if (darkTheme) DarkAmeenColors else LightAmeenColors

    // Enforce RTL for proper Arabic rendering and provide Ameen Design Tokens
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalAmeenColors provides ameenColors,
        LocalAmeenSpacing provides AmeenSpacingTokens(),
        LocalAmeenShapes provides AmeenShapeTokens(),
        LocalAmeenElevation provides AmeenElevationTokens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
