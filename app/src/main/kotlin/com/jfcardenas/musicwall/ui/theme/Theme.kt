package com.jfcardenas.musicwall.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = Purple,
    onPrimary        = Color.White,
    primaryContainer = PurpleDark,
    background       = Background,
    surface          = Surface,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    surfaceVariant   = Card,
    outline          = CardBorder,
)

@Composable
fun MusicWallTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = MusicWallTypography,
        content     = content,
    )
}
