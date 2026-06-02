package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.api.ArtistItem
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.ArtistasViewModel
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel
import com.jfcardenas.musicwall.ui.viewmodel.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    coverVm: CoverInteractionViewModel,
    onGoToFavoritas: () -> Unit,
    onGoToCuenta: () -> Unit,
    vm: HomeViewModel = hiltViewModel(),
    artistsVm: ArtistasViewModel = hiltViewModel(),
) {
    val state = vm.uiState
    val artistState = artistsVm.uiState
    var showRecentTracks by remember { mutableStateOf(false) }
    var showArtists by remember { mutableStateOf(false) }
    val favoriteKeys = coverVm.favoriteKeys

    LaunchedEffect(favoriteKeys) {
        vm.applyFavoriteKeys(favoriteKeys)
    }

    if (showRecentTracks) {
        ModalBottomSheet(
            onDismissRequest = { showRecentTracks = false },
            containerColor = Color(0xFF1A1A1A),
        ) {
            RecentScrobblesSheet(tracks = state.recentTracks)
        }
    }

    if (showArtists) {
        ModalBottomSheet(
            onDismissRequest = { showArtists = false },
            containerColor = Color(0xFF1A1A1A),
        ) {
            ArtistsSheet(
                artists = artistState.topArtists,
                artistImages = artistState.artistImages,
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Purple.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.avatarUrl != null) {
                        AsyncImage(
                            model = state.avatarUrl,
                            contentDescription = state.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Text(
                            state.username.take(1).uppercase().ifBlank { "M" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple,
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biblioteca", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    Text(
                        text = if (state.username.isNotBlank()) "last.fm/${state.username}" else "Conecta tu cuenta",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                }
                TextButton(onClick = onGoToCuenta) {
                    Text("Cuenta", color = Purple)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ProfileStat("Scrobbles", state.playcount, Modifier.weight(1f), onClick = { showRecentTracks = true })
                ProfileStat("Artistas", state.artistCount, Modifier.weight(1f), onClick = { showArtists = true })
            }
        }

        item {
            OutlinedButton(
                onClick = onGoToFavoritas,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Ver mis portadas favoritas", color = Purple)
            }
        }
    }
}

@Composable
private fun ArtistsSheet(
    artists: List<ArtistItem>,
    artistImages: Map<String, String>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Tus artistas",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        if (artists.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Text("Sin artistas", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(artists, key = { it.name }) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Card)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val imageUrl = artistImages[artist.name]
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Purple.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (imageUrl != null) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Text(artist.name.take(1), color = Purple, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(artist.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("${artist.playcount} scrobbles", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentScrobblesSheet(tracks: List<RecentTrack>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Scrobbles recientes",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        if (tracks.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Text("Sin scrobbles recientes", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tracks, key = { "${it.artist.name}-${it.name}-${it.date?.uts}" }) { track ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Card)
                            .padding(12.dp),
                    ) {
                        Text(track.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text(track.artist.name, fontSize = 12.sp, color = TextSecondary)
                        Text(
                            relativeTime(track.date?.uts),
                            fontSize = 11.sp,
                            color = TextMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Card,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
