package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.jfcardenas.musicwall.api.Album
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.UserTag
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.CoverSearchState
import com.jfcardenas.musicwall.ui.components.LayeredAlbumCover
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.CoverItem
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel
import com.jfcardenas.musicwall.ui.viewmodel.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGoToEstilos: () -> Unit = {},
    onGoToHistorial: () -> Unit = {},
    onGoToArtistas: () -> Unit = {},
    onGoToFavoritas: () -> Unit = {},
    onNowPlayingClick: (artist: String, track: String) -> Unit = { _, _ -> },
    vm: HomeViewModel = hiltViewModel(),
) {
    val state = vm.uiState
    var showRecentSheet by remember { mutableStateOf(false) }

    if (showRecentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecentSheet = false },
            containerColor   = Color(0xFF1A1A1A),
        ) {
            RecentTracksSheet(tracks = state.recentTracks)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh    = { vm.refresh() },
        modifier     = Modifier.fillMaxSize(),
    ) {
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
                    val timerMs = (state.songDurationMs / 3L).takeIf { it > 15_000L } ?: 70_000L
                    SkullTimer(
                        durationMs = timerMs,
                        onRefresh  = { vm.refresh() },
                    )
                }
            }

            // Now Playing card
            item {
                NowPlayingCard(
                    track            = state.currentTrack,
                    coverUrl         = state.currentTrackCoverUrl,
                    coverSearchState = state.coverSearchState,
                    isNowPlaying     = state.isNowPlaying,
                    isLoading        = state.isLoading,
                    error            = state.error,
                    onSearchCover    = { vm.searchCurrentCover() },
                    onClick = {
                        state.currentTrack?.let { t ->
                            onNowPlayingClick(t.artist.name, t.name)
                        }
                    },
                )
            }

            // Stats
            if (state.playcount != "—" || state.artistCount != "—") {
                item {
                    StatsCard(
                        playcount        = state.playcount,
                        artistCount      = state.artistCount,
                        onScrobblesClick = { showRecentSheet = true },
                        onArtistasClick  = onGoToArtistas,
                    )
                }
            }

            // Géneros favoritos
            if (state.topTags.isNotEmpty()) {
                item {
                    GenresCard(tags = state.topTags)
                }
            }

            // Top de la semana → crear mural
            item {
                WeekAlbumsCard(
                    albums         = state.weekAlbums,
                    coverOverrides = state.coverOverrides,
                    onClick        = onGoToEstilos,
                    modifier       = Modifier.fillMaxWidth(),
                )
            }

            // Recomendaciones basadas en lo que más escuchas
            if (state.recommendations.isNotEmpty()) {
                item {
                    RecommendationsCard(
                        albums         = state.recommendations,
                        coverOverrides = state.coverOverrides,
                        onAlbumClick   = { album ->
                            onNowPlayingClick(album.artist.name, album.name)
                        },
                    )
                }
            }

            // Mis portadas favoritas
            item {
                QuickAccessCard(
                    emoji    = "♡",
                    title    = "Mis portadas",
                    subtitle = "Las que guardaste",
                    onClick  = onGoToFavoritas,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // VS de portadas — random de toda la DB
            item {
                VsCard(coverA = state.vsCoverA, coverB = state.vsCoverB)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun GenresCard(tags: List<UserTag>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = "Tus géneros",
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary,
            modifier   = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tags.take(10)) { tag ->
                val count = tag.count.toIntOrNull() ?: 0
                val alpha = if (count > 0) (0.4f + 0.6f * (count.coerceAtMost(100) / 100f)).coerceIn(0.4f, 1f) else 0.6f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Purple.copy(alpha = alpha * 0.25f))
                        .border(1.dp, Purple.copy(alpha = alpha * 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text     = tag.name,
                        fontSize = 12.sp,
                        color    = Purple.copy(alpha = alpha),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationsCard(
    albums: List<Album>,
    coverOverrides: Map<String, String>,
    onAlbumClick: (Album) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text       = "Te puede gustar",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
                Text(
                    text     = "Basado en tus artistas del mes",
                    fontSize = 11.sp,
                    color    = TextSecondary,
                )
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(albums) { album ->
                val key      = "${album.artist.name}::${album.name}".lowercase()
                val imageUrl = coverOverrides[key] ?: album.images.getExtraLargeUrl()
                Column(
                    modifier            = Modifier
                        .width(100.dp)
                        .clickable { onAlbumClick(album) },
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Surface),
                    ) {
                        if (imageUrl != null) {
                            AsyncImage(
                                model              = imageUrl,
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize(),
                            )
                        } else {
                            Box(
                                modifier         = Modifier.fillMaxSize().background(Color(0xFF1A0A2E)),
                                contentAlignment = Alignment.Center,
                            ) { Text("♫", fontSize = 22.sp, color = Purple) }
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text     = album.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color    = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text     = album.artist.name,
                        fontSize = 10.sp,
                        color    = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
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
    coverUrl: String?,
    coverSearchState: CoverSearchState,
    isNowPlaying: Boolean,
    isLoading: Boolean,
    error: String?,
    onSearchCover: () -> Unit,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .then(if (track != null) Modifier.clickable { onClick() } else Modifier),
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
                    TrackContent(
                        track            = track,
                        coverUrl         = coverUrl,
                        coverSearchState = coverSearchState,
                        isNowPlaying     = isNowPlaying,
                        onSearchCover    = onSearchCover,
                    )
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
private fun TrackContent(
    track: RecentTrack,
    coverUrl: String?,
    coverSearchState: CoverSearchState,
    isNowPlaying: Boolean,
    onSearchCover: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LayeredAlbumCover(
            imageUrl     = coverUrl,
            cornerRadius = 10.dp,
            modifier     = Modifier.size(80.dp),
        )
        when (coverSearchState) {
            CoverSearchState.Idle -> if (coverUrl == null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "Buscar portada ↗",
                    fontSize = 9.sp,
                    color    = Purple,
                    modifier = Modifier.clickable(onClick = onSearchCover),
                )
            }
            CoverSearchState.Searching -> {
                Spacer(Modifier.height(4.dp))
                CircularProgressIndicator(modifier = Modifier.size(10.dp), color = Purple, strokeWidth = 1.5.dp)
            }
            CoverSearchState.NotFound -> {
                Spacer(Modifier.height(4.dp))
                Text("sin portada", fontSize = 9.sp, color = TextMuted)
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

    Spacer(Modifier.height(12.dp))

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
private fun StatsCard(
    playcount: String,
    artistCount: String,
    onScrobblesClick: () -> Unit,
    onArtistasClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatBox(
            label    = "Scrobbles",
            value    = formatNumber(playcount),
            modifier = Modifier.weight(1f),
            onClick  = onScrobblesClick,
        )
        StatBox(
            label    = "Artistas",
            value    = formatNumber(artistCount),
            modifier = Modifier.weight(1f),
            onClick  = onArtistasClick,
        )
        StatBox(
            label    = "Murales",
            value    = "—",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = value,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text     = label,
                    fontSize = 11.sp,
                    color    = TextSecondary,
                )
                if (onClick != null) {
                    Text(text = "›", fontSize = 11.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun RecentTracksSheet(tracks: List<RecentTrack>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text       = "Últimas canciones",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary,
            modifier   = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        if (tracks.isEmpty()) {
            Box(
                modifier            = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment    = Alignment.Center,
            ) {
                Text("Sin historial reciente", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(tracks) { track -> RecentTrackRow(track) }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

@Composable
private fun RecentTrackRow(track: RecentTrack) {
    val isNowPlaying = track.attr?.nowplaying == "true"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isNowPlaying) Card else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val imageUrl = track.images.getExtraLargeUrl()
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Surface),
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model            = imageUrl,
                    contentDescription = null,
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier         = Modifier.fillMaxSize().background(Color(0xFF1A0A2E)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("♫", fontSize = 16.sp, color = Purple)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = track.name,
                fontSize = 14.sp,
                fontWeight = if (isNowPlaying) FontWeight.SemiBold else FontWeight.Normal,
                color    = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = buildString {
                append(track.artist.name)
                if (track.album.name.isNotBlank()) append(" · ${track.album.name}")
            }
            Text(
                text     = subtitle,
                fontSize = 12.sp,
                color    = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (isNowPlaying) {
            Box(modifier = Modifier.size(6.dp).background(Color(0xFF1DB954), CircleShape))
        } else {
            Text(
                text     = relativeTime(track.date?.uts),
                fontSize = 10.sp,
                color    = TextMuted,
            )
        }
    }
}

@Composable
private fun WeekAlbumsCard(
    albums: List<Album>,
    coverOverrides: Map<String, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padded: List<Album?> = (albums.take(8) + List(8) { null as Album? }).take(8)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Crear mural", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Tus top esta semana", fontSize = 11.sp, color = TextSecondary)
                }
                Text("›", fontSize = 18.sp, color = TextMuted)
            }
            Spacer(Modifier.height(10.dp))
            padded.chunked(4).forEachIndexed { rowIdx, row ->
                if (rowIdx > 0) Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEachIndexed { colIdx, album ->
                        val key      = album?.let { "${it.artist.name}::${it.name}".lowercase() }
                        val imageUrl = key?.let { coverOverrides[it] } ?: album?.images?.getExtraLargeUrl()
                        AlbumThumb(
                            imageUrl = imageUrl,
                            index    = rowIdx * 4 + colIdx,
                            modifier = Modifier.weight(1f).height(64.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumThumb(imageUrl: String?, index: Int = 0, modifier: Modifier = Modifier) {
    var visible by remember(imageUrl) { mutableStateOf(false) }
    LaunchedEffect(imageUrl) {
        visible = false
        delay(index * 45L)
        visible = true
    }
    val scale by animateFloatAsState(
        targetValue    = if (visible) 1f else 0.72f,
        animationSpec  = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label          = "thumb_scale",
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(160),
        label         = "thumb_alpha",
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .clip(RoundedCornerShape(6.dp))
            .background(Surface),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun VsCard(
    coverA: CoverItem?,
    coverB: CoverItem?,
    modifier: Modifier = Modifier,
) {
    if (coverA == null || coverB == null) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "¿Cuál define tu semana?",
                fontSize = 11.sp,
                color = TextMuted,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VsCoverItem(cover = coverA, modifier = Modifier.weight(1f))
                Text(
                    text = "VS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                )
                VsCoverItem(cover = coverB, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun VsCoverItem(cover: CoverItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface),
        ) {
            AsyncImage(
                model = cover.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = cover.albumName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = cover.artistName,
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── Skull refresh timer ────────────────────────────────────────────────────────

@Composable
private fun SkullTimer(
    durationMs: Long,
    onRefresh: () -> Unit,
) {
    var key      by remember { mutableStateOf(0) }
    val progress = remember { Animatable(1f) }
    var scream   by remember { mutableStateOf(false) }

    LaunchedEffect(key) {
        progress.snapTo(1f); scream = false
        progress.animateTo(0f, tween(durationMs.toInt(), easing = LinearEasing))
        scream = true; delay(360L); onRefresh(); scream = false; key++
    }

    val scale by animateFloatAsState(
        targetValue   = if (scream) 1.45f else 1f,
        animationSpec = spring(dampingRatio = 0.22f, stiffness = 550f),
        label         = "lml_scale",
    )

    Text(
        text     = "🤘",
        fontSize = 17.sp,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha  = 0.35f + 0.65f * progress.value
            }
            .clickable { onRefresh(); key++ }
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

private fun formatNumber(raw: String): String {
    val n = raw.toLongOrNull() ?: return raw
    return when {
        n >= 1_000_000 -> "${n / 1_000_000}M"
        n >= 1_000     -> "${n / 1_000}K"
        else           -> n.toString()
    }
}
