package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  LIGHT,
  DARK,
  SYSTEM
}

@Immutable
data class CustomStatusColors(
  val success: Color,
  val successContainer: Color,
  val warning: Color,
  val warningContainer: Color,
  val danger: Color,
  val dangerContainer: Color,
  val textSecondary: Color,
  val border: Color
)

val LocalStatusColors = staticCompositionLocalOf {
  CustomStatusColors(
    success = LightSuccess,
    successContainer = LightSuccessContainer,
    warning = LightWarning,
    warningContainer = LightWarningContainer,
    danger = LightDanger,
    dangerContainer = LightDangerContainer,
    textSecondary = LightTextSecondary,
    border = LightBorder
  )
}

val StatusColors: CustomStatusColors
  @Composable
  get() = LocalStatusColors.current

private val LightColorScheme = lightColorScheme(
  primary = LightPrimary,
  onPrimary = Color.White,
  primaryContainer = LightPrimaryContainer,
  onPrimaryContainer = LightOnPrimaryContainer,
  secondary = LightSecondary,
  onSecondary = Color.White,
  background = LightBackground,
  onBackground = LightText,
  surface = LightSurface,
  onSurface = LightText,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  error = LightDanger,
  onError = Color.White,
  errorContainer = LightDangerContainer,
  onErrorContainer = LightDanger
)

private val DarkColorScheme = darkColorScheme(
  primary = DarkPrimary,
  onPrimary = Color(0xFF0B1220),
  primaryContainer = DarkPrimaryContainer,
  onPrimaryContainer = DarkOnPrimaryContainer,
  secondary = DarkSecondary,
  onSecondary = Color(0xFF0B1220),
  background = DarkBackground,
  onBackground = DarkText,
  surface = DarkSurface,
  onSurface = DarkText,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  error = DarkDanger,
  onError = Color(0xFF0B1220),
  errorContainer = DarkDangerContainer,
  onErrorContainer = DarkDanger
)

@Composable
fun KhmKasbonTheme(
  themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  content: @Composable () -> Unit
) {
  val isDark = when (themeMode) {
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  val statusColors = if (isDark) {
    CustomStatusColors(
      success = DarkSuccess,
      successContainer = DarkSuccessContainer,
      warning = DarkWarning,
      warningContainer = DarkWarningContainer,
      danger = DarkDanger,
      dangerContainer = DarkDangerContainer,
      textSecondary = DarkTextSecondary,
      border = DarkBorder
    )
  } else {
    CustomStatusColors(
      success = LightSuccess,
      successContainer = LightSuccessContainer,
      warning = LightWarning,
      warningContainer = LightWarningContainer,
      danger = LightDanger,
      dangerContainer = LightDangerContainer,
      textSecondary = LightTextSecondary,
      border = LightBorder
    )
  }

  CompositionLocalProvider(LocalStatusColors provides statusColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}

