package com.denoise.denoiseapp.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography

private val White = Color(0xFFFFFFFF)
private val Black = Color(0xFF0F1115)
private val Gray1 = Color(0xFF1A1D22)
private val Gray2 = Color(0xFF2A2F36)
private val Gray3 = Color(0xFFEEF0F3)
private val Gray4 = Color(0xFF959BA4)
private val Accent = Color(0xFF3AA0FF) // acento muy sutil para focus/acciones

private val LightScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    secondary = Gray4,
    onSecondary = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Gray3,
    onSurfaceVariant = Gray4,
    outline = Gray4
)

private val DarkScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Gray4,
    onSecondary = White,
    background = Gray1,
    onBackground = White,
    surface = Gray1,
    onSurface = White,
    surfaceVariant = Gray2,
    onSurfaceVariant = Gray4,
    outline = Gray4
)

private val DenoiseTypography = Typography() // usa defaults Material3

@Composable
fun DenoiseTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = scheme.copy(
            // acentos sutiles (focus/pressed)
            tertiary = Accent,
            onTertiary = White
        ),
        typography = DenoiseTypography,
        content = content
    )
}
