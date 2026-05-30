package com.jfcardenas.musicwall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jfcardenas.musicwall.ui.theme.Background

private val albumPalette = listOf(
    Color(0xFF2D1B69), Color(0xFF8B1A1A), Color(0xFF1A3A5C), Color(0xFF3D2B1F),
    Color(0xFF1A4A2A), Color(0xFF4A2A4A), Color(0xFF5C3A00), Color(0xFF2A4A5C),
    Color(0xFF3A1A4A), Color(0xFF5C1A2A), Color(0xFF1A5C3A), Color(0xFF4A3A1A),
    Color(0xFF6B3FAF), Color(0xFF1A2A6C), Color(0xFF6C1A1A), Color(0xFF1A6C4A),
    Color(0xFF4A1A6C), Color(0xFF6C4A1A), Color(0xFF1A4A6C), Color(0xFF6C1A4A),
)

@Composable
fun AlbumGridBackground(modifier: Modifier = Modifier) {
    val cols = 4
    val rows = 10

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(52.dp),
        ) {
            repeat(rows) { row ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(cols) { col ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(albumPalette[(row * cols + col) % albumPalette.size])
                        )
                    }
                }
            }
        }

        // Top scrim: darkens status bar area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(androidx.compose.ui.Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Background, Background.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
        )

        // Bottom scrim: fades into background for button area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Background.copy(alpha = 0.92f), Background)
                    )
                )
        )
    }
}
