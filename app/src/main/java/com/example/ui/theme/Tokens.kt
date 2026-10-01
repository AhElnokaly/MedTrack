package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-Contrast Medical Design Tokens for Ameen (أمين).
 * Provides unified, accessible tokens for Colors, Spacing, Shapes, and Elevation
 * across Light and Dark themes.
 */
data class AmeenColorTokens(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val cardBorder: Color,
    val borderFocused: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnBrand: Color,
    val criticalRed: Color,
    val criticalRedLight: Color,
    val warningAmber: Color,
    val warningAmberLight: Color,
    val successGreen: Color,
    val successGreenLight: Color,
    val isDark: Boolean
)

val LightAmeenColors = AmeenColorTokens(
    primary = Color(0xFF007A64), // Rich emerald
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6F7F5), // Mint container
    onPrimaryContainer = Color(0xFF004D40),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC), // Slate-50 clean canvas
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9), // Slate-100
    border = Color(0xFFCBD5E1), // Slate-300
    cardBorder = Color(0xFFCBD5E1),
    borderFocused = Color(0xFF007A64),
    textPrimary = Color(0xFF0F172A), // Slate-900 high contrast
    textSecondary = Color(0xFF334155), // Slate-700
    textMuted = Color(0xFF64748B), // Slate-500
    textOnBrand = Color.White,
    criticalRed = Color(0xFFDC2626),
    criticalRedLight = Color(0xFFFEE2E2),
    warningAmber = Color(0xFFD97706),
    warningAmberLight = Color(0xFFFEF3C7),
    successGreen = Color(0xFF16A34A),
    successGreenLight = Color(0xFFDCFCE7),
    isDark = false
)

val DarkAmeenColors = AmeenColorTokens(
    primary = Color(0xFF14B8A6), // Vibrant teal-emerald readable against dark surfaces
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF064E3B), // Deep emerald container
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    background = Color(0xFF0B1715), // Deep dark green-slate canvas
    surface = Color(0xFF142422), // Elevated dark card surface
    surfaceVariant = Color(0xFF1D312E), // Distinct elevated container
    border = Color(0xFF2D4540), // Crisp dark border
    cardBorder = Color(0xFF2D4540),
    borderFocused = Color(0xFF2DD4BF),
    textPrimary = Color(0xFFF8FAFC), // Crisp white-slate
    textSecondary = Color(0xFFCBD5E1), // Readable light slate
    textMuted = Color(0xFF94A3B8), // Medium muted
    textOnBrand = Color.White,
    criticalRed = Color(0xFFF87171),
    criticalRedLight = Color(0xFF450A0A),
    warningAmber = Color(0xFFFBBF24),
    warningAmberLight = Color(0xFF451A03),
    successGreen = Color(0xFF4ADE80),
    successGreenLight = Color(0xFF052E16),
    isDark = true
)

data class AmeenSpacingTokens(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
    val screenPadding: Dp = 20.dp,
    val cardPadding: Dp = 16.dp,
    val itemSpacing: Dp = 10.dp
)

data class AmeenShapeTokens(
    val none: Shape = RoundedCornerShape(0.dp),
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val large: Shape = RoundedCornerShape(16.dp),
    val card: Shape = RoundedCornerShape(18.dp),
    val dialog: Shape = RoundedCornerShape(24.dp),
    val pill: Shape = CircleShape
)

data class AmeenElevationTokens(
    val none: Dp = 0.dp,
    val low: Dp = 2.dp,
    val medium: Dp = 4.dp,
    val high: Dp = 8.dp,
    val dialog: Dp = 16.dp
)

val LocalAmeenColors = staticCompositionLocalOf { LightAmeenColors }
val LocalAmeenSpacing = staticCompositionLocalOf { AmeenSpacingTokens() }
val LocalAmeenShapes = staticCompositionLocalOf { AmeenShapeTokens() }
val LocalAmeenElevation = staticCompositionLocalOf { AmeenElevationTokens() }

/**
 * Accessor object for Ameen Design Tokens.
 */
object AmeenTheme {
    val colors: AmeenColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAmeenColors.current

    val spacing: AmeenSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAmeenSpacing.current

    val shapes: AmeenShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAmeenShapes.current

    val elevation: AmeenElevationTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalAmeenElevation.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAmeenColors.current.isDark
}
