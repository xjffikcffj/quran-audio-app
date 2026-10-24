package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

val IslamicDarkColorScheme = darkColorScheme(
    primary = IslamicGold,
    onPrimary = Color(0xFF1E1700),
    primaryContainer = IslamicEmeraldDark,
    onPrimaryContainer = IslamicGoldLight,
    secondary = IslamicEmeraldLight,
    onSecondary = Color(0xFF003823),
    secondaryContainer = IslamicSurfaceVariant,
    onSecondaryContainer = IslamicTextPrimary,
    tertiary = IslamicGoldLight,
    onTertiary = Color(0xFF261D00),
    background = IslamicDarkBg,
    onBackground = IslamicTextPrimary,
    surface = IslamicSurface,
    onSurface = IslamicTextPrimary,
    surfaceVariant = IslamicSurfaceVariant,
    onSurfaceVariant = IslamicTextSecondary,
    outline = IslamicBorder,
    outlineVariant = IslamicDivider,
    error = IslamicRed,
    onError = Color.White
)

val PureNightColorScheme = darkColorScheme(
    primary = PureNightGold,
    onPrimary = Color(0xFF120E00),
    primaryContainer = Color(0xFF061510),
    onPrimaryContainer = PureNightGoldLight,
    secondary = IslamicEmerald,
    onSecondary = Color(0xFF002214),
    secondaryContainer = PureNightSurfaceVariant,
    onSecondaryContainer = PureNightTextPrimary,
    tertiary = PureNightGoldLight,
    onTertiary = Color(0xFF161100),
    background = PureNightBg,
    onBackground = PureNightTextPrimary,
    surface = PureNightSurface,
    onSurface = PureNightTextPrimary,
    surfaceVariant = PureNightSurfaceVariant,
    onSurfaceVariant = PureNightTextSecondary,
    outline = PureNightBorder,
    outlineVariant = PureNightDivider,
    error = IslamicRed,
    onError = Color.White
)

val IslamicLightColorScheme = lightColorScheme(
    primary = LightGold,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightEmeraldDark,
    secondary = LightEmerald,
    onSecondary = Color.White,
    secondaryContainer = LightCardHover,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightGoldLight,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightDivider,
    error = IslamicRed,
    onError = Color.White
)

@Composable
fun QuranAudioTheme(
    mode: AppThemeMode? = null,
    content: @Composable () -> Unit
) {
    val activeMode by ThemeManager.themeMode.collectAsState()
    val currentMode = mode ?: activeMode

    val colorScheme = when (currentMode) {
        AppThemeMode.DARK -> IslamicDarkColorScheme
        AppThemeMode.PURE_NIGHT -> PureNightColorScheme
        AppThemeMode.LIGHT -> IslamicLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
