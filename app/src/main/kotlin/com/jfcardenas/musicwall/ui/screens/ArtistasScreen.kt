package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.jfcardenas.musicwall.api.Album
import com.jfcardenas.musicwall.api.ArtistItem
import com.jfcardenas.musicwall.api.UserTag
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.ArtistasViewModel

@Composable
fun ArtistasScreen(
    onBack: () -> Unit,
    vm: ArtistasViewModel = hiltViewModel(),
) {
    val state = vm.uiState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
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
                    text = "Artistas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Purple, strokeWidth = 2.dp)
                }
            }
            return@LazyColumn
        }

        // Álbumes del mes
        if (state.topAlbums.isNotEmpty()) {
            item {
                Text(
                    text = "Álbumes del mes",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 10.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.topAlbums) { album ->
                        AlbumCard(album, overrideUrl = state.coverOverrides["${album.artist.name}::${album.name}".lowercase()])
                    }
                }
            }
        }

        // Artistas más escuchados
        if (state.topArtists.isNotEmpty()) {
            item {
                Text(
                    text = "Artistas más escuchados",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
                )
            }
            itemsIndexed(state.topArtists) { index, artist ->
                val imageUrl = artist.images?.getExtraLargeUrl()?.takeIf { it.isNotBlank() }
                    ?: state.artistImages[artist.name]
                ArtistRow(rank = index + 1, artist = artist, imageUrl = imageUrl)
            }
        }

        // Géneros favoritos
        if (state.topTags.isNotEmpty()) {
            item {
                Text(
                    text = "Tus géneros",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 10.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.topTags) { tag -> GenreChip(tag) }
                }
            }
        }

        // Recomendaciones
        if (state.recommendations.isNotEmpty()) {
            item {
                Text(
                    text = "Quizás te guste",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 2.dp),
                )
                Text(
                    text = "Basado en tus artistas más escuchados",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 10.dp),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.recommendations) { album ->
                        AlbumCard(album, overrideUrl = state.coverOverrides["${album.artist.name}::${album.name}".lowercase()])
                    }
                }
            }
        }

        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun AlbumCard(album: Album, overrideUrl: String? = null) {
    val imageUrl = album.images.getExtraLargeUrl()?.takeIf { it.isNotBlank() }
        ?: coverArtUrl(album.mbid)
        ?: overrideUrl
    Column(
        modifier = Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface),
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF2D1B69), Color(0xFF1A0A2E)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("♫", fontSize = 24.sp, color = Purple)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = album.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = album.artist.name,
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ArtistRow(rank: Int, artist: ArtistItem, imageUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$rank",
            fontSize = 13.sp,
            color = TextMuted,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(24.dp),
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Purple.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                Text(
                    text = artist.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleLight,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (artist.playcount != null) {
                Text(
                    text = "${formatPlaycount(artist.playcount)} escuchas",
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun GenreChip(tag: UserTag) {
    val count = tag.count.toIntOrNull() ?: 0
    val alpha = (0.35f + 0.65f * (count.toFloat() / 500f).coerceIn(0f, 1f))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Purple.copy(alpha = alpha))
            .border(1.dp, Purple.copy(alpha = (alpha + 0.2f).coerceIn(0f, 1f)), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text       = tag.name,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = Color.White.copy(alpha = 0.9f),
        )
    }
}

private fun coverArtUrl(mbid: String?): String? =
    mbid?.takeIf { it.isNotBlank() }
        ?.let { "https://coverartarchive.org/release/$it/front-250" }

private fun formatPlaycount(raw: String): String {
    val n = raw.toLongOrNull() ?: return raw
    return when {
        n >= 1_000_000 -> "${n / 1_000_000}M"
        n >= 1_000     -> "${n / 1_000}K"
        else           -> n.toString()
    }
}
