package com.jfcardenas.musicwall.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.jfcardenas.musicwall.ui.theme.Background
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ALL_BACKGROUNDS = listOf(
    "samples/37510d9b-3056-4042-9dd9-38aa9c029ab3.jpg",
    "samples/451c6113-a60a-4a7d-be93-dbb5f367eefc.jpg",
    "samples/503705d3-4814-4510-b792-6dcc38b644d4.png",
    "samples/53557121-9ab4-4d18-babc-1a073e7b2504.png",
    "samples/5f56075b-bf9f-4329-a682-ee921658f3bc.jpg",
    "samples/64ff25d9-2072-41e7-84b1-c9a635c17b5d.png",
    "samples/752a2d00-6eec-4705-80fd-0b127a1fd622.png",
    "samples/a6b0bba0-3f3a-4bed-aadf-113616d2287c.jpg",
    "samples/abf057d7-9496-4707-a1aa-2b938dcd0c9d.jpg",
    "samples/b58b52f2-847c-4ca9-a4a2-b2008d5844e0.jpg",
    "samples/ChatGPT Image 30 may 2026, 04_13_58 p.m..png",
)

@Composable
fun SampleBackground(route: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val assetPath = remember(route) { ALL_BACKGROUNDS.random() }

    val bitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, assetPath) {
        value = assetPath?.let {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open(it).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = bitmap,
            animationSpec = tween(500),
            label = "sample_bg",
        ) { bmp ->
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize().background(Background))
            }
        }

        // Subtle vignette so edges don't compete with content
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x28000000))
        )

        // Top scrim for status bar / header area
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.22f)
                .align(androidx.compose.ui.Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(Color(0xCC000000), Color.Transparent))
                )
        )

        // Bottom scrim for navigation bar area
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.18f)
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))
                )
        )
    }
}
