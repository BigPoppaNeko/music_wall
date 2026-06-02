package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.jfcardenas.musicwall.api.Album
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.browseableGenreTags
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.CoverSearchState
import com.jfcardenas.musicwall.ui.components.AlbumCoverPreviewOverlay
import com.jfcardenas.musicwall.ui.components.InteractiveAlbumCover
import com.jfcardenas.musicwall.ui.components.LayeredAlbumCover
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel
import com.jfcardenas.musicwall.ui.viewmodel.CoverItem
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel
import com.jfcardenas.musicwall.ui.viewmodel.relativeTime

private const val DiscoveryGestureHint = "Doble toque actualizar · triple vetar · mantén portada y canciones"
private const val GenreGestureHint = "Toque ampliar · doble toque otro género · mantén portada y canciones"
private const val CoverGestureHint = "Mantén portada y canciones"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    coverVm: CoverInteractionViewModel,
    onGoToEstilos: () -> Unit = {},
    onGoToFavoritas: () -> Unit = {},
    onNowPlayingClick: (artist: String, track: String, album: String) -> Unit = { _, _, _ -> },
    vm: HomeViewModel = hiltViewModel(),
) {
    val recommendationCovers by remember { derivedStateOf { vm.uiState.recommendationCovers } }
    val genreRecommendations by remember { derivedStateOf { vm.uiState.genreRecommendations } }
    val isLoading by remember { derivedStateOf { vm.uiState.isLoading } }
    val state by remember { derivedStateOf { vm.uiState } }
    val lifecycleOwner = LocalLifecycleOwner.current
    val favoriteKeys = coverVm.favoriteKeys
    var genreRefreshKey by remember { mutableIntStateOf(0) }
    var manualGenre by remember { mutableStateOf<String?>(null) }
    var genrePreviewCover by remember { mutableStateOf<CoverItem?>(null) }

    LaunchedEffect(favoriteKeys) {
        vm.applyFavoriteKeys(favoriteKeys)
    }

    DisposableEffect(lifecycleOwner) {
        var firstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (firstResume) {
                    firstResume = false
                } else {
                    vm.reloadRecommendationCovers()
                    genreRefreshKey++
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(genreRefreshKey) {
        manualGenre = null
    }

    val browseableTags = remember(state.topTags) { browseableGenreTags(state.topTags) }
    val autoGenre = remember(browseableTags, genreRefreshKey) {
        browseableTags.shuffled().firstOrNull()
    }
    val activeGenre = manualGenre ?: autoGenre

    LaunchedEffect(activeGenre, genreRefreshKey, manualGenre) {
        activeGenre?.let { genre ->
            vm.loadGenreRecommendations(
                genre = genre,
                excludeKeys = favoriteKeys,
                limit = 16,
                forceRefresh = manualGenre != null,
            )
        }
    }

    val genreCovers by remember(activeGenre, genreRecommendations) {
        derivedStateOf {
            activeGenre?.lowercase()?.let { genreRecommendations[it] }.orEmpty()
        }
    }

    val openCoverDetail: (CoverItem, Boolean) -> Unit = { cover, showVeto ->
        genrePreviewCover = null
        coverVm.openCoverOverlay(
            cover = cover,
            showVeto = showVeto,
            onVeto = if (showVeto) {
                { vetoed -> vm.vetoAndRefreshRecommendations(vetoed) }
            } else {
                null
            },
            onFavorited = { favorited -> vm.removeFromDiscoveryFeeds(favorited.key) },
        )
    }

    val shuffleGenre: () -> Unit = {
        if (browseableTags.isNotEmpty()) {
            val next = browseableTags
                .filterNot { it.equals(activeGenre, ignoreCase = true) }
                .randomOrNull()
                ?: browseableTags.random()
            manualGenre = next
        }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh    = { vm.refresh() },
        modifier     = Modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize()) {
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
                    artistReferences = state.currentArtistCovers,
                    favoriteKeys     = favoriteKeys,
                    onSearchCover    = { vm.searchCurrentCover() },
                    onShowDetail = {
                        state.currentTrack?.let { t ->
                            val albumName = t.album.name.takeIf { it.isNotBlank() } ?: t.name
                            openCoverDetail(
                                CoverItem(
                                    imageUrl = state.currentTrackCoverUrl.orEmpty(),
                                    albumName = albumName,
                                    artistName = t.artist.name,
                                ),
                                false,
                            )
                        }
                    },
                    onShowDetailCover = { openCoverDetail(it, false) },
                    onClick = {
                        state.currentTrack?.let { t ->
                            onNowPlayingClick(t.artist.name, t.name, t.album.name)
                        }
                    },
                )
            }

            if (recommendationCovers.isNotEmpty()) {
                item {
                    RecommendationsCard(
                        covers           = recommendationCovers,
                        favoriteKeys     = favoriteKeys,
                        isRefreshing     = state.isRefreshingRecommendations,
                        onRefreshCovers  = { vm.reloadRecommendationCovers() },
                        onShowDetail     = { openCoverDetail(it, true) },
                        onVetoCover      = { vm.vetoAndRefreshRecommendations(it) },
                    )
                }
            }

            item {
                VsCard(
                    coverA           = state.vsCoverA,
                    coverB           = state.vsCoverB,
                    favoriteKeys     = favoriteKeys,
                    onPick           = { vm.chooseVsWinner(it) },
                    onShowDetail     = { openCoverDetail(it, false) },
                    streaks          = state.vsWinStreaks,
                    winnerKey        = state.lastVsWinnerKey,
                )
            }

            if (activeGenre != null) {
                item {
                    GenreExplorationSection(
                        activeGenre = activeGenre,
                        genreCovers = genreCovers,
                        isLoading = state.loadingGenre == activeGenre.lowercase(),
                        favoriteKeys = favoriteKeys,
                        onShuffleGenre = shuffleGenre,
                        onPreviewCover = { genrePreviewCover = it },
                        onShowDetail = { openCoverDetail(it, false) },
                    )
                }
            }

            if (state.favoriteCovers.isNotEmpty()) {
                item {
                    FavoriteCoversStrip(
                        covers = state.favoriteCovers,
                        favoriteKeys = favoriteKeys,
                        onOpenAll = onGoToFavoritas,
                        onShowDetail = { openCoverDetail(it, false) },
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        genrePreviewCover?.let { cover ->
            AlbumCoverPreviewOverlay(
                cover = cover,
                onDismiss = { genrePreviewCover = null },
            )
        }
        }
    }
}

@Composable
private fun RecommendationsCard(
    covers: List<CoverItem>,
    favoriteKeys: Set<String>,
    isRefreshing: Boolean,
    onRefreshCovers: () -> Unit,
    onShowDetail: (CoverItem) -> Unit,
    onVetoCover: (CoverItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Te puede gustar",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                )
                Text(
                    text     = DiscoveryGestureHint,
                    fontSize = 11.sp,
                    color    = TextSecondary,
                )
            }
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = Purple,
                    strokeWidth = 1.5.dp,
                )
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(covers, key = { it.key }) { coverItem ->
                Column(
                    modifier            = Modifier.width(100.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    InteractiveAlbumCover(
                        imageUrl             = coverItem.imageUrl,
                        contentDescription   = coverItem.albumName,
                        isFavorite           = coverItem.key in favoriteKeys,
                        modifier             = Modifier.size(100.dp),
                        onShowDetail         = { onShowDetail(coverItem) },
                        onDoubleTap          = onRefreshCovers,
                        onTripleTap          = { onVetoCover(coverItem) },
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text     = coverItem.albumName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color    = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text     = coverItem.artistName,
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
private fun NowPlayingCard(
    track: RecentTrack?,
    coverUrl: String?,
    coverSearchState: CoverSearchState,
    isNowPlaying: Boolean,
    isLoading: Boolean,
    error: String?,
    artistReferences: List<CoverItem>,
    favoriteKeys: Set<String>,
    onSearchCover: () -> Unit,
    onShowDetail: () -> Unit,
    onShowDetailCover: (CoverItem) -> Unit,
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
                        artistReferences = artistReferences,
                        favoriteKeys     = favoriteKeys,
                        onSearchCover    = onSearchCover,
                        onShowDetail     = onShowDetail,
                        onShowDetailCover = onShowDetailCover,
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
    artistReferences: List<CoverItem>,
    favoriteKeys: Set<String>,
    onSearchCover: () -> Unit,
    onShowDetail: () -> Unit,
    onShowDetailCover: (CoverItem) -> Unit,
) {
    val albumName = track.album.name.takeIf { it.isNotBlank() } ?: track.name
    val coverKey = "${track.artist.name}::${albumName}".lowercase()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (coverUrl != null) {
                InteractiveAlbumCover(
                    imageUrl           = coverUrl,
                    contentDescription = albumName,
                    isFavorite         = coverKey in favoriteKeys,
                    modifier           = Modifier.size(80.dp),
                    cornerRadius       = 10.dp,
                    onShowDetail       = onShowDetail,
                )
            } else {
                LayeredAlbumCover(
                    imageUrl     = null,
                    cornerRadius = 10.dp,
                    modifier     = Modifier.size(80.dp),
                )
            }
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

        if (artistReferences.isNotEmpty()) {
            Spacer(Modifier.width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                artistReferences.take(2).forEach { cover ->
                    InteractiveAlbumCover(
                        imageUrl           = cover.imageUrl,
                        contentDescription = cover.albumName,
                        isFavorite         = cover.key in favoriteKeys,
                        modifier           = Modifier.size(42.dp),
                        cornerRadius       = 7.dp,
                        onShowDetail       = { onShowDetailCover(cover) },
                    )
                }
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
private fun VsCard(
    coverA: CoverItem?,
    coverB: CoverItem?,
    favoriteKeys: Set<String>,
    onPick: (CoverItem) -> Unit,
    onShowDetail: (CoverItem) -> Unit,
    streaks: Map<String, Int>,
    winnerKey: String?,
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
                VsCoverItem(
                    cover = coverA,
                    onPick = { onPick(coverA) },
                    onShowDetail = { onShowDetail(coverA) },
                    streak = streaks[coverA.key] ?: 0,
                    isWinner = winnerKey == coverA.key,
                    isFavorited = coverA.key in favoriteKeys,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "VS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                )
                VsCoverItem(
                    cover = coverB,
                    onPick = { onPick(coverB) },
                    onShowDetail = { onShowDetail(coverB) },
                    streak = streaks[coverB.key] ?: 0,
                    isWinner = winnerKey == coverB.key,
                    isFavorited = coverB.key in favoriteKeys,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun VsCoverItem(
    cover: CoverItem,
    onPick: () -> Unit,
    onShowDetail: () -> Unit,
    streak: Int,
    isWinner: Boolean,
    isFavorited: Boolean,
    modifier: Modifier = Modifier,
) {
    val winnerScale by animateFloatAsState(
        targetValue = if (isWinner) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "winner_scale",
    )
    val lmlScale by animateFloatAsState(
        targetValue = if (isWinner) 1f else 0.65f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "winner_lml_scale",
    )
    val lmlAlpha by animateFloatAsState(
        targetValue = if (streak > 0) 1f else 0f,
        animationSpec = tween(160),
        label = "winner_lml_alpha",
    )
    val streakColor = when {
        streak >= 10 -> Color(0xFFFFD166)
        streak >= 5  -> Color(0xFFFF4D8D)
        streak >= 3  -> Purple
        else         -> Purple.copy(alpha = 0.9f)
    }
    val lmlText = when {
        streak >= 10 -> "🤘🔥"
        streak >= 5  -> "🤘⚡"
        else         -> "🤘"
    }
    val lmlFontSize = when {
        streak >= 10 -> 24.sp
        streak >= 5  -> 23.sp
        streak >= 3  -> 22.sp
        else         -> 20.sp
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = winnerScale
                    scaleY = winnerScale
                }
                .clip(RoundedCornerShape(10.dp))
                .background(Surface),
        ) {
            InteractiveAlbumCover(
                imageUrl           = cover.imageUrl,
                contentDescription = cover.albumName,
                isFavorite         = isFavorited,
                modifier           = Modifier.fillMaxSize(),
                onShowDetail       = onShowDetail,
                onClick            = onPick,
            )
        }
        if (streak > 0) {
            Spacer(Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = lmlAlpha
                        scaleX = lmlScale
                        scaleY = lmlScale
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, streakColor.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = lmlText, fontSize = lmlFontSize, color = streakColor)
                Text("x$streak", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }
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

@Composable
private fun GenreExplorationSection(
    activeGenre: String,
    genreCovers: List<CoverItem>,
    isLoading: Boolean,
    favoriteKeys: Set<String>,
    onShuffleGenre: () -> Unit,
    onPreviewCover: (CoverItem) -> Unit,
    onShowDetail: (CoverItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Explorando $activeGenre",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                Text(
                    text = GenreGestureHint,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Purple,
                    strokeWidth = 2.dp,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        HomeGenreCovers(
            covers = genreCovers,
            favoriteKeys = favoriteKeys,
            onShuffleGenre = onShuffleGenre,
            onPreviewCover = onPreviewCover,
            onShowDetail = onShowDetail,
        )
    }
}

@Composable
private fun HomeGenreCovers(
    covers: List<CoverItem>,
    favoriteKeys: Set<String>,
    onShuffleGenre: () -> Unit,
    onPreviewCover: (CoverItem) -> Unit,
    onShowDetail: (CoverItem) -> Unit,
) {
    val visibleCovers = remember(covers, favoriteKeys) {
        covers.filter { it.key !in favoriteKeys }.distinctBy { it.key }.take(8)
    }

    if (visibleCovers.isEmpty()) {
        Text(
            text = "Sin portadas para este género · doble toque para cambiar",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onShuffleGenre() })
            },
        )
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(visibleCovers, key = { it.key }) { coverItem ->
            Column(
                modifier = Modifier.width(100.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                InteractiveAlbumCover(
                    imageUrl           = coverItem.imageUrl,
                    contentDescription = coverItem.albumName,
                    isFavorite         = coverItem.key in favoriteKeys,
                    modifier           = Modifier.size(100.dp),
                    onShowDetail       = { onShowDetail(coverItem) },
                    onClick            = { onPreviewCover(coverItem) },
                    onDoubleTap        = onShuffleGenre,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = coverItem.albumName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = coverItem.artistName,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FavoriteCoversStrip(
    covers: List<CoverItem>,
    favoriteKeys: Set<String>,
    onOpenAll: () -> Unit,
    onShowDetail: (CoverItem) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Favoritas", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(CoverGestureHint, fontSize = 11.sp, color = TextSecondary)
            }
            Text(
                text = "Ver todas",
                fontSize = 12.sp,
                color = Purple,
                modifier = Modifier.clickable { onOpenAll() },
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(covers, key = { it.key }) { cover ->
                InteractiveAlbumCover(
                    imageUrl           = cover.imageUrl,
                    contentDescription = cover.albumName,
                    isFavorite         = cover.key in favoriteKeys,
                    modifier           = Modifier.size(76.dp),
                    cornerRadius       = 12.dp,
                    onShowDetail       = { onShowDetail(cover) },
                    onClick            = { onOpenAll() },
                )
            }
        }
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
        text     = "🥁↻🥁",
        fontSize = 16.sp,
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
