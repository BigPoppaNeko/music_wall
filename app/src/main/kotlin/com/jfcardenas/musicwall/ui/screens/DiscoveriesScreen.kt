package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfcardenas.musicwall.ui.theme.*

private data class Discovery(
    val id: String,
    val title: String,
    val subtitle: String,
    val mood: String,
    val styleId: String,
    val gradientStart: Color,
    val gradientEnd: Color,
)

private val DISCOVERIES = listOf(
    Discovery("d1", "Para los noctámbulos", "Colores que fluyen en la oscuridad", "Ambient · Trip-hop", "ecosystem",
        Color(0xFF1A0A2E), Color(0xFF6B3FAF)),
    Discovery("d2", "Caos urbano", "Texturas que gritan en silencio", "Metal · Punk · Noise", "street",
        Color(0xFF5C1A00), Color(0xFF8B3A00)),
    Discovery("d3", "Entre constelaciones", "Tu música entre las estrellas", "Post-rock · Shoegaze", "constellation",
        Color(0xFF05051A), Color(0xFF1A1A6C)),
    Discovery("d4", "Arte contemplativo", "Silencio visual en galería", "Classical · Jazz · Minimal", "museum",
        Color(0xFF121218), Color(0xFF2A2A3A)),
    Discovery("d5", "Sueño eléctrico", "Sintetizadores y neón", "Electronic · Synthwave", "ecosystem",
        Color(0xFF0A002A), Color(0xFF2A003A)),
    Discovery("d6", "Tierra y raíces", "Los colores que te forjaron", "Folk · Blues · Soul", "street",
        Color(0xFF3A2000), Color(0xFF5C3A10)),
    Discovery("d7", "Lluvia interior", "Melancolía convertida en paleta", "Indie · Alternative", "constellation",
        Color(0xFF0A1A2A), Color(0xFF1A2A3A)),
    Discovery("d8", "Claridad", "Luz blanca entre notas", "Pop · Singer-songwriter", "museum",
        Color(0xFF1A1A22), Color(0xFF3A3A4A)),
)

@Composable
fun DiscoveriesScreen(
    onBack: () -> Unit,
    onDiscover: (styleId: String) -> Unit,
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
                    text = "Descubrimientos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Nuevas ideas para tus murales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(DISCOVERIES) { discovery ->
                DiscoveryCard(discovery = discovery, onClick = { onDiscover(discovery.styleId) })
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun DiscoveryCard(discovery: Discovery, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(discovery.gradientStart, discovery.gradientEnd)
                )
            )
            .clickable { onClick() },
    ) {
        // Subtle grid texture overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.03f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Mood tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = discovery.mood,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                )
            }

            Column {
                Text(
                    text = discovery.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = discovery.subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.65f),
                )
            }
        }

        // CTA chip bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Generar →",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}
