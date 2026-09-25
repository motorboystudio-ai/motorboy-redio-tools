package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = TechPrimary,
    onPrimary = TechOnPrimary,
    primaryContainer = TechPrimaryContainer,
    onPrimaryContainer = TechOnPrimaryContainer,
    secondary = TechSecondary,
    onSecondary = TechOnSecondary,
    secondaryContainer = TechSecondaryContainer,
    onSecondaryContainer = TechOnSecondaryContainer,
    tertiary = TechTertiary,
    onTertiary = TechOnTertiary,
    tertiaryContainer = TechTertiaryContainer,
    onTertiaryContainer = TechOnTertiaryContainer,
    error = TechError,
    onError = TechOnError,
    errorContainer = TechErrorContainer,
    onErrorContainer = TechOnErrorContainer,
    background = TechBackground,
    onBackground = TechOnSurface,
    surface = TechSurface,
    onSurface = TechOnSurface,
    surfaceVariant = TechSurfaceVariant,
    onSurfaceVariant = TechOnSurface,
    surfaceContainer = TechSurfaceContainer,
    surfaceContainerHigh = TechSurfaceContainerHigh,
    outline = TechOutline,
    outlineVariant = TechOutlineVariant
  )

private val LightColorScheme = lightColorScheme(
  primary = TechPrimary,
  onPrimary = TechOnPrimary,
  primaryContainer = TechPrimaryContainer,
  onPrimaryContainer = TechOnPrimaryContainer,
  secondary = Color(0xFF006875),
  onSecondary = Color(0xFFFFFFFF),
  secondaryContainer = Color(0xFF97F0FF),
  onSecondaryContainer = Color(0xFF001F24),
  tertiary = Color(0xFF725C00),
  onTertiary = Color(0xFFFFFFFF),
  tertiaryContainer = Color(0xFFFFE07D),
  onTertiaryContainer = Color(0xFF231B00),
  error = TechError,
  onError = TechOnError,
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0B0E14),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF0B0E14),
  surfaceVariant = Color(0xFFE2E8F0),
  onSurfaceVariant = Color(0xFF1E293B),
  outline = Color(0xFF94A3B8),
  outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}


