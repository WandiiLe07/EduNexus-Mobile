package com.dut.edunexusmobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// EduNexus Brand Colours
// ============================================================

// Main brand colour
val ForestCode = Color(0xFF0B3D2E)

// Soft secondary colour
val MintFog = Color(0xFFD9F7E7)

// Small accent/highlight colour
val LaserLemon = Color(0xFFCCFF00)

// ============================================================
// Light Mode
// ============================================================

private val LightColors = lightColorScheme(

    primary = ForestCode,
    onPrimary = Color.White,

    primaryContainer = MintFog,
    onPrimaryContainer = ForestCode,

    secondary = Color(0xFF287A59),
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFE5F6ED),
    onSecondaryContainer = ForestCode,

    tertiary = Color(0xFF536B00),
    onTertiary = Color.White,

    tertiaryContainer = Color(0xFFF2FFC1),
    onTertiaryContainer = Color(0xFF243000),

    background = Color(0xFFFBFDFB),
    onBackground = Color(0xFF151A17),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF151A17),

    surfaceVariant = Color(0xFFF0F4F1),
    onSurfaceVariant = Color(0xFF59645E),

    outline = Color(0xFFBEC8C1),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

// ============================================================
// Dark Mode
// ============================================================

private val DarkColors = darkColorScheme(

    primary = Color(0xFF80E5AA),
    onPrimary = Color(0xFF003921),

    primaryContainer = ForestCode,
    onPrimaryContainer = MintFog,

    secondary = Color(0xFF9AD6B5),
    onSecondary = Color(0xFF073924),

    secondaryContainer = Color(0xFF173F30),
    onSecondaryContainer = Color(0xFFD5F4E2),

    tertiary = LaserLemon,
    onTertiary = Color(0xFF1C2500),

    tertiaryContainer = Color(0xFF354500),
    onTertiaryContainer = Color(0xFFE8FF91),

    background = Color(0xFF101411),
    onBackground = Color(0xFFE5E9E5),

    surface = Color(0xFF151A17),
    onSurface = Color(0xFFE5E9E5),

    surfaceVariant = Color(0xFF202722),
    onSurfaceVariant = Color(0xFFBBC5BE),

    outline = Color(0xFF77827B),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ============================================================
// Theme
// ============================================================

@Composable
fun EduNexusMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors =
        if (darkTheme) {
            DarkColors
        } else {
            LightColors
        }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}