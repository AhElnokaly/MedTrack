package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Static base colors for constants
val StaticEmeraldPrimary = Color(0xFF007A64)
val StaticEmeraldDark = Color(0xFF004D40)
val StaticMintSecondary = Color(0xFF0D9488)
val StaticBackgroundLight = Color(0xFFF8FAFC)
val StaticSurfaceLight = Color(0xFFFFFFFF)

// High-Contrast Health Brand Palette (Theme-aware via Tokens)
val EmeraldPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.primary

val EmeraldDark: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.onPrimaryContainer

val EmeraldLight: Color
    @Composable
    @ReadOnlyComposable
    get() = if (AmeenTheme.isDark) Color(0xFF0F3832) else Color(0xFFE6F4F1)

val MintSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.secondary

val MintLight: Color
    @Composable
    @ReadOnlyComposable
    get() = if (AmeenTheme.isDark) Color(0xFF134E48) else Color(0xFFCCFBF1)

val MintContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.primaryContainer

val SageAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = if (AmeenTheme.isDark) Color(0xFF2DD4BF) else Color(0xFF14B8A6)

// Critical & Status Colors (High-Contrast)
val CoralRed = Color(0xFFE11D48)

val CriticalRed: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.criticalRed

val CriticalRedDark = Color(0xFF991B1B)

val CriticalRedLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.criticalRedLight

val WarningAmber: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.warningAmber

val WarningAmberDark = Color(0xFF92400E)

val WarningAmberLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.warningAmberLight

val SuccessGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.successGreen

val SuccessGreenLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.successGreenLight

// Surfaces & Backgrounds (Clean, high visual separation)
val BackgroundLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.background

val SurfaceLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.surface

val SurfaceVariantLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.surfaceVariant

val CardBorderColor: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.cardBorder

val BorderLight: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.border

val BorderFocused: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.borderFocused

// High-Contrast Typography Colors
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.textMuted

val TextOnBrand: Color
    @Composable
    @ReadOnlyComposable
    get() = AmeenTheme.colors.textOnBrand

// Dark Theme Palette Constants
val BackgroundDark = Color(0xFF0B1715)
val SurfaceDark = Color(0xFF142422)
val SurfaceVariantDark = Color(0xFF1D312E)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFFCBD5E1)

