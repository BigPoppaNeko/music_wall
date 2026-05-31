package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.api.MasterVersion
import com.jfcardenas.musicwall.data.CoverSearchState
import com.jfcardenas.musicwall.ui.components.LayeredAlbumCover
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.NowPlayingDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingDetailScreen(
    artist: String,
    trackName: String,
    onBack: () -> Unit,
    vm: NowPlayingDetailViewModel = hiltViewModel(),
) {
    val state = vm.uiState
    var showVersionsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(artist, trackName) {
        vm.load(artist = artist, track = trackName)
    }

    if (showVersionsSheet && state.versions.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showVersionsSheet = false },
            containerColor = Color(0xFF1A1A1A),
        ) {
            VersionsSheet(
                albumTitle = state.albumTitle ?: trackName,
                versions = state.versions,
                totalVersions = state.totalVersions,
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
            Text(
                text = trackName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (state.albumTitle != null) {
                IconButton(onClick = { vm.toggleFavorite() }) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (state.isFavorite) "Quitar de favoritas" else "Guardar en favoritas",
                        tint = if (state.isFavorite) Color(0xFFE05C6A) else TextSecondary,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LayeredAlbumCover(
            imageUrl     = state.albumImageUrl,
            cornerRadius = 16.dp,
            modifier     = Modifier
                .padding(horizontal = 48.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
        )

        // Botón fallback de portada
        when {
            state.coverReady && state.albumImageUrl == null
                && state.coverFallback == CoverSearchState.Idle -> {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick   = { vm.fetchCoverFallback() },
                    modifier  = Modifier.align(Alignment.CenterHorizontally),
                    shape     = RoundedCornerShape(20.dp),
                    border    = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                ) {
                    Text("Buscar portada en otra fuente ↗", fontSize = 12.sp, color = TextSecondary)
                }
            }
            state.coverFallback == CoverSearchState.Searching -> {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Purple, strokeWidth = 2.dp)
                }
            }
            state.coverFallback == CoverSearchState.NotFound -> {
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = "Portada no disponible",
                    fontSize = 12.sp,
                    color    = TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = trackName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = artist,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(28.dp))

        // Letra
        DetailCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            SectionHeader(icon = "🎵", title = "Letra")
            Spacer(Modifier.height(12.dp))
            when {
                state.isLoadingLyrics     -> LoadingCenter()
                state.lyricsError != null -> ErrorText(state.lyricsError)
                state.lyrics != null      -> Text(
                    text = state.lyrics,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Curiosidad
        DetailCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionHeader(icon = "✦", title = "Curiosidad")
                if (state.curiositySource != null) {
                    Spacer(Modifier.width(8.dp))
                    SourceChip(state.curiositySource)
                }
            }
            Spacer(Modifier.height(12.dp))
            when {
                state.isLoadingCuriosity     -> LoadingCenter()
                state.curiosityError != null -> ErrorText(state.curiosityError)
                state.curiosity != null      -> Text(
                    text = state.curiosity,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp,
                )
            }
        }

        // Formatos físicos (Discogs) — tappable si hay versiones
        if (state.formatGroups.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            DetailCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = if (state.versions.isNotEmpty()) {
                    { showVersionsSheet = true }
                } else null,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionHeader(icon = "📦", title = "Formatos físicos")
                    Spacer(Modifier.weight(1f))
                    if (state.versions.isNotEmpty()) {
                        Text(
                            text = "ver ediciones ›",
                            fontSize = 10.sp,
                            color = PurpleLight,
                            fontWeight = FontWeight.Medium,
                        )
                    } else {
                        Text("Discogs", fontSize = 9.sp, color = TextMuted)
                    }
                }
                if (state.totalVersions > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${state.totalVersions} versiones en el mercado",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }
                Spacer(Modifier.height(12.dp))
                // Imágenes reales por formato (vinyl, CD, cassette)
                val hasFormatImages = state.vinylThumb != null || state.cdThumb != null || state.cassetteThumb != null
                if (hasFormatImages) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.vinylThumb?.let    { FormatImageCard(thumb = it, label = "Vinilo",  icon = "♦") }
                        state.cdThumb?.let       { FormatImageCard(thumb = it, label = "CD",      icon = "●") }
                        state.cassetteThumb?.let { FormatImageCard(thumb = it, label = "Casete",  icon = "⬛") }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.formatGroups.forEach { group ->
                            FormatChip(icon = group.type.icon, label = group.type.label)
                        }
                    }
                }
                state.firstEdition?.let { fe ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = buildString {
                            append("Primera edición")
                            if (fe.year != null) append(": ${fe.year}")
                            if (fe.country != null) append(" · ${fe.country}")
                            if (fe.label != null) append(" · ${fe.label}")
                        },
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
}

// ── Bottom sheet de ediciones ─────────────────────────────────────────────────

@Composable
private fun VersionsSheet(
    albumTitle: String,
    versions: List<MasterVersion>,
    totalVersions: Int,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = albumTitle,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        if (totalVersions > 0) {
            Text(
                text = "$totalVersions ediciones registradas",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
        ) {
            items(versions) { version -> VersionCell(version) }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun VersionCell(version: MasterVersion) {
    val hasThumb = !version.thumb.isNullOrBlank()
        && version.thumb != "https://st.discogs.com/"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface),
            contentAlignment = Alignment.Center,
        ) {
            if (hasThumb) {
                AsyncImage(
                    model = version.thumb,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val abbr = when {
                    version.format?.contains("LP") == true       -> "LP"
                    version.format?.contains("Cass") == true     -> "MC"
                    version.format?.contains("CD") == true       -> "CD"
                    version.format?.contains("File") == true     -> "↓"
                    else                                          -> "—"
                }
                Text(abbr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PurpleLight)
            }
        }
        Spacer(Modifier.height(3.dp))
        val info = buildString {
            if (!version.country.isNullOrBlank()) append(version.country)
            if (!version.released.isNullOrBlank()) {
                if (isNotEmpty()) append(" · ")
                append(version.released)
            }
        }
        if (info.isNotBlank()) {
            Text(
                text = info,
                fontSize = 9.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!version.label.isNullOrBlank()) {
            Text(
                text = version.label,
                fontSize = 8.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── Componentes reutilizables ─────────────────────────────────────────────────

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(20.dp),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SectionHeader(icon: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun SourceChip(source: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Purple.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text("de la $source", fontSize = 10.sp, color = PurpleLight, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FormatImageCard(thumb: String, label: String, icon: String) {
    Column(
        modifier = Modifier.width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Surface),
        ) {
            AsyncImage(
                model              = thumb,
                contentDescription = label,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(5.dp))
        Row(
            verticalAlignment       = Alignment.CenterVertically,
            horizontalArrangement   = Arrangement.spacedBy(3.dp),
        ) {
            Text(icon, fontSize = 10.sp, color = PurpleLight)
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FormatChip(icon: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardBorder)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(icon, fontSize = 11.sp)
        Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LoadingCenter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Purple, strokeWidth = 2.dp)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        fontSize = 13.sp,
        color = TextMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    )
}
