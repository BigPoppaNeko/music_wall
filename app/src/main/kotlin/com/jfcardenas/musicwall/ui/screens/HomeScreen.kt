package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.ui.model.LOADING_QUOTES
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel
import com.jfcardenas.musicwall.ui.viewmodel.relativeTime

@Composable
fun HomeScreen(
    onGoToDescubrimientos: () -> Unit = {},
    onGoToHistorial: () -> Unit = {},
    vm: HomeViewModel = hiltViewModel(),
) {
    val state = vm.uiState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Tu música",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    if (state.username.isNotEmpty()) {
                        Text(
                            text = "last.fm/${state.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                IconButton(onClick = { vm.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = TextSecondary)
                }
            }
        }

        // Now Playing card
        item {
            NowPlayingCard(
                track = state.currentTrack,
                isNowPlaying = state.isNowPlaying,
                isLoading = state.isLoading,
                error = state.error,
            )
        }

        // Stats
        if (state.playcount != "—" || state.artistCount != "—") {
            item {
                StatsCard(playcount = state.playcount, artistCount = state.artistCount)
            }
        }

        // Quote
        item {
            val quote = remember { LOADING_QUOTES.random() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Card)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        text = quote.text,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        color = TextSecondary,
                        lineHeight = 22.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "— ${quote.author}",
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                    )
                }
            }
        }

        // Quick access
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickAccessCard(
                    emoji = "✦",
                    title = "Descubrimientos",
                    subtitle = "Nuevas ideas",
                    onClick = onGoToDescubrimientos,
                    modifier = Modifier.weight(1f),
                )
                QuickAccessCard(
                    emoji = "🖼",
                    title = "Historial",
                    subtitle = "Tus murales",
                    onClick = onGoToHistorial,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuickAccessCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Column {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun NowPlayingCard(
    track: RecentTrack?,
    isNowPlaying: Boolean,
    isLoading: Boolean,
    error: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isNowPlaying) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF1DB954), CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = if (isNowPlaying) "Ahora sonando" else "Último scrobble",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isNowPlaying) Color(0xFF1DB954) else TextSecondary,
                    letterSpacing = 0.5.sp,
                )
                if (isNowPlaying) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "· Scrobble en tiempo real",
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Purple, strokeWidth = 2.dp)
                    }
                }
                error != null -> {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    )
                }
                track != null -> {
                    TrackContent(track = track, isNowPlaying = isNowPlaying)
                }
                else -> {
                    Text(
                        text = "Sin actividad reciente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackContent(track: RecentTrack, isNowPlaying: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Album art
        val imageUrl = track.images.getExtraLargeUrl()
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface),
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Portada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0xFF2D1B69), Color(0xFF1A0A2E)))
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("♫", fontSize = 28.sp, color = Purple)
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.artist.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (track.album.name.isNotBlank()) {
                Text(
                    text = track.album.name,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    // Decorative progress bar
    Spacer(Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(CardBorder),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (isNowPlaying) 0.44f else 1f)
                .background(Brush.horizontalGradient(listOf(Purple, PurpleLight)))
        )
    }

    Spacer(Modifier.height(10.dp))

    if (!isNowPlaying) {
        Text(
            text = "Último scrobble ${relativeTime(track.date?.uts)}",
            fontSize = 12.sp,
            color = TextMuted,
        )
    } else {
        Text(
            text = "Escuchando ahora ♪",
            fontSize = 12.sp,
            color = Color(0xFF1DB954),
        )
    }
}

@Composable
private fun StatsCard(playcount: String, artistCount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatBox(
            label = "Scrobbles",
            value = formatNumber(playcount),
            modifier = Modifier.weight(1f),
        )
        StatBox(
            label = "Artistas",
            value = formatNumber(artistCount),
            modifier = Modifier.weight(1f),
        )
        StatBox(
            label = "Murales",
            value = "—",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
            )
        }
    }
}

private fun formatNumber(raw: String): String {
    val n = raw.toLongOrNull() ?: return raw
    return when {
        n >= 1_000_000 -> "${n / 1_000_000}M"
        n >= 1_000     -> "${n / 1_000}K"
        else           -> n.toString()
    }
}
