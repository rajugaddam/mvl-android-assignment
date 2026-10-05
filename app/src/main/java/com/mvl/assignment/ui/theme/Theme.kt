package com.mvl.assignment.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Fixed light scheme matching the Figma design (no dynamic color, so every device looks the same). */
private val MvlColorScheme = lightColorScheme(
    primary = MvlYellow,
    onPrimary = MvlBlack,
    secondary = MvlBlack,
    onSecondary = MvlWhite,
    background = MvlWhite,
    onBackground = MvlBlack,
    surface = MvlWhite,
    onSurface = MvlBlack,
    surfaceVariant = MvlGreyLabel,
    onSurfaceVariant = MvlGreyText,
    outline = MvlGreyBorder,
    outlineVariant = MvlGreyDivider,
    error = MvlRed,
    onError = MvlWhite,
    scrim = MvlScrim,
)

@Composable
fun MvlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MvlColorScheme,
        typography = Typography,
        content = content,
    )
}
