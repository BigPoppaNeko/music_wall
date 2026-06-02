package com.jfcardenas.musicwall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jfcardenas.musicwall.data.local.db.entity.FavoriteAlbum
import com.jfcardenas.musicwall.ui.components.InteractiveAlbumCover
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel
import com.jfcardenas.musicwall.ui.viewmodel.CoverItem
import com.jfcardenas.musicwall.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    coverVm: CoverInteractionViewModel,
    vm: FavoritesViewModel = hiltViewModel(),
) {
    val albums = vm.albums
    val favoriteKeys = coverVm.favoriteKeys

    LaunchedEffect(favoriteKeys) {
        vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
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
            Column {
                Text(
                    text       = "Mis portadas",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                )
                if (albums.isNotEmpty()) {
                    Text(
                        text     = "${albums.size} guardadas",
                        fontSize = 12.sp,
                        color    = TextSecondary,
                    )
                }
            }
        }

        if (albums.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♡", fontSize = 48.sp, color = TextMuted)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text       = "Aún no hay portadas guardadas",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextSecondary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text      = "Doble toque en cualquier portada\npara guardarla aquí.",
                        fontSize  = 13.sp,
                        color     = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxSize(),
            ) {
                items(albums, key = { it.id }) { album ->
                    val cover = CoverItem(
                        imageUrl = album.imageUrl.orEmpty(),
                        albumName = album.albumName,
                        artistName = album.artistName,
                    )
                    FavoriteAlbumCard(
                        album        = album,
                        cover        = cover,
                        isFavorite   = album.id in favoriteKeys,
                        onShowDetail = { coverVm.openAlbumDetail(cover) },
                    )
                }
                item(span = { GridItemSpan(2) }) { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

@Composable
private fun FavoriteAlbumCard(
    album: FavoriteAlbum,
    cover: CoverItem,
    isFavorite: Boolean,
    onShowDetail: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        if (album.imageUrl != null) {
            InteractiveAlbumCover(
                imageUrl           = cover.imageUrl,
                contentDescription = cover.albumName,
                isFavorite         = isFavorite,
                modifier           = Modifier.fillMaxSize(),
                cornerRadius       = 14.dp,
                onShowDetail       = onShowDetail,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF2D1B69), Color(0xFF1A0A2E)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("♫", fontSize = 32.sp, color = Purple)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.75f))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
        ) {
            Text(
                text       = cover.albumName,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                text     = cover.artistName,
                fontSize = 10.sp,
                color    = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
