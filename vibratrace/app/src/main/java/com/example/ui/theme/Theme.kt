package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AgriGreenPrimaryDark,
    onPrimary = Color(0xFF00390B),
    primaryContainer = Color(0xFF005315),
    onPrimaryContainer = Color(0xFFA6F59D),
    secondary = AgriBlueSecondaryDark,
    onSecondary = Color(0xFF00344F),
    secondaryContainer = Color(0xFF004C70),
    onSecondaryContainer = Color(0xFFC7E7FF),
    background = AgriBackgroundDark,
    onBackground = Color(0xFFE1E4DE),
    surface = AgriSurfaceDark,
    onSurface = Color(0xFFE1E4DE),
    surfaceVariant = AgriSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC2C9BE),
    outline = Color(0xFF8C9388)
)

private val LightColorScheme = lightColorScheme(
    primary = AgriGreenPrimary,
    onPrimary = AgriGreenOnPrimary,
    primaryContainer = AgriGreenContainer,
    onPrimaryContainer = AgriGreenOnContainer,
    secondary = AgriBlueSecondary,
    onSecondary = AgriBlueOnSecondary,
    secondaryContainer = AgriBlueContainer,
    onSecondaryContainer = AgriBlueOnContainer,
    tertiary = AgriAmberTertiary,
    tertiaryContainer = AgriAmberContainer,
    background = AgriBackgroundLight,
    onBackground = Color(0xFF0F172A),          // Slate 900: pitch-sharp readability
    surface = AgriSurfaceLight,
    onSurface = Color(0xFF0F172A),             // Slate 900: high-contrast on white card surfaces
    surfaceVariant = AgriSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF334155),      // Slate 700: clear, readable secondary text
    outline = AgriOutlineLight,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun VibraTraceTheme(
    darkTheme: Boolean = false, // Always default to crisp, clean light mode for supreme legibility
    dynamicColor: Boolean = false, // Keep consistent agricultural green brand palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VibraTraceTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
