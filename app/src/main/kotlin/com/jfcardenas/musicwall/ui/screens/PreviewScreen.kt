package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.PreviewViewModel
import java.io.File

@Composable
fun PreviewScreen(
    styleId: String,
    onBack: () -> Unit,
    onRegenerate: () -> Unit,
    onApply: () -> Unit,
    onShare: () -> Unit,
    vm: PreviewViewModel = hiltViewModel(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
            Column {
                Text(
                    text = "Tu mural está listo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Así se verá tu wallpaper.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        // Wallpaper preview inside a phone-frame box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Phone frame
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(9f / 19.5f)
                    .border(2.dp, CardBorder, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Card),
            ) {
                val path = vm.collagePath
                if (path != null) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = "Tu mural",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(styleGradient(styleId))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(styleEmoji(styleId), fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = styleName(styleId),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                // Status bar scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )
            }
        }

        // Quote
        Text(
            text = "\"El alma se tiñe del color de sus pensamientos.\"\n— Marco Aurelio",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp),
        )

        // Action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Regenerar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp),
            ) {
                IconButton(onClick = onRegenerate) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerar", tint = TextSecondary)
                }
                Text(text = "Regenerar", fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }

            // Aplicar wallpaper
            Button(
                onClick = { vm.applyWallpaper() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
            ) {
                Text(
                    text = "Aplicar wallpaper",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }

            // Compartir
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp),
            ) {
                IconButton(onClick = { vm.shareWallpaper() }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir", tint = TextSecondary)
                }
                Text(text = "Compartir", fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

private fun styleGradient(id: String): List<Color> = when (id) {
    "street"    -> listOf(Color(0xFF5C2A0A), Color(0xFF2A1205), Color(0xFF1A0A04))
    "cinematic" -> listOf(Color(0xFF0A0A3A), Color(0xFF05051A), Color(0xFF02020D))
    "album"     -> listOf(Color(0xFF1A1A22), Color(0xFF0E0E14), Color(0xFF080808))
    "physical"  -> listOf(Color(0xFF2A1C0A), Color(0xFF140E04), Color(0xFF0A0702))
    else        -> listOf(Color(0xFF0A1A3A), Color(0xFF0A0A1E), Color(0xFF050510))
}

private fun styleEmoji(id: String): String = when (id) {
    "street"    -> "🎭"
    "cinematic" -> "✦"
    "album"     -> "🖼"
    "physical"  -> "📦"
    else        -> "🌿"
}

private fun styleName(id: String): String = when (id) {
    "street"    -> "Street Poster"
    "cinematic" -> "Cinematic"
    "album"     -> "Album Wall"
    "physical"  -> "Physical"
    else        -> "Ecosystem"
}
