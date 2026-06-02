package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
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
                    Text(styleEmoji(styleId), fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = styleName(styleId),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.62f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.74f))))
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Column {
                Text(
                    text = "Aleatorio infinito",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "La escena vuelve a nacer con cada dado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.68f),
                )
            }
        }

        DiceButton(
            onClick = onRegenerate,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    vm.applyWallpaper()
                    onApply()
                },
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp),
            ) {
                IconButton(onClick = { vm.shareWallpaper() }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White.copy(alpha = 0.78f))
                }
                Text(text = "Compartir", fontSize = 11.sp, color = Color.White.copy(alpha = 0.78f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DiceButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(58.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.58f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("🎲", fontSize = 28.sp)
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
