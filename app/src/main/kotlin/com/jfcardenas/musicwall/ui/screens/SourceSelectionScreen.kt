package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfcardenas.musicwall.ui.theme.*

enum class MusicSource { LASTFM, SPOTIFY, EXPLORE }

@Composable
fun SourceSelectionScreen(
    onBack: (() -> Unit)? = null,
    onSourceSelected: (MusicSource) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "¿De dónde quieres\ntraer tu música?",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Elige tu fuente para crear tu mural.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(36.dp))

            SourceCard(
                icon = { LastFmIcon() },
                title = "Last.fm",
                description = "Usa tu historial de scrobbles en tiempo real",
                onClick = { onSourceSelected(MusicSource.LASTFM) },
            )

            Spacer(Modifier.height(12.dp))

            SourceCard(
                icon = { SpotifyIcon() },
                title = "Spotify",
                description = "Pega el enlace de una playlist pública",
                onClick = { onSourceSelected(MusicSource.SPOTIFY) },
            )

            Spacer(Modifier.height(12.dp))

            SourceCard(
                icon = { ExploreIcon() },
                title = "Explorar",
                description = "Elige tus artistas o álbumes favoritos",
                onClick = { onSourceSelected(MusicSource.EXPLORE) },
            )

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = "🔒", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "No guardamos tus contraseñas. Todo es local y privado.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = TextMuted,
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SourceCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        Text(text = "›", fontSize = 22.sp, color = TextSecondary)
    }
}

@Composable
private fun LastFmIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFFD92323), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "as", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
private fun SpotifyIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFF1DB954), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "♫", fontSize = 22.sp, color = Color.Black)
    }
}

@Composable
private fun ExploreIcon() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color(0xFF2A2A3A), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "♡", fontSize = 22.sp, color = Purple)
    }
}
