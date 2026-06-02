package com.jfcardenas.musicwall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.api.formatTotalDuration
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.AlbumDetailState

private val OverlayCardShape = RoundedCornerShape(22.dp)
private val CoverImageShape = RoundedCornerShape(14.dp)

@Composable
fun AlbumTracksOverlay(
    detail: AlbumDetailState,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSeconds = detail.tracks.sumOf { it.durationSeconds }
    val totalDuration = formatTotalDuration(totalSeconds)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .widthIn(max = 360.dp)
                .fillMaxHeight(0.92f)
                .shadow(
                    elevation = 32.dp,
                    shape = OverlayCardShape,
                    ambientColor = Color.Black.copy(alpha = 0.55f),
                    spotColor = Color.Black.copy(alpha = 0.75f),
                )
                .clip(OverlayCardShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2A2E),
                            Color(0xFF1A1A1E),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = OverlayCardShape,
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = CoverImageShape,
                            ambientColor = Color.Black.copy(alpha = 0.45f),
                            spotColor = Color.Black.copy(alpha = 0.65f),
                        )
                        .clip(CoverImageShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = detail.cover.imageUrl,
                        contentDescription = detail.cover.albumName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(14.dp)) }

            item {
                Text(
                    text = detail.cover.albumName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                Text(
                    text = detail.cover.artistName,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item { Spacer(Modifier.height(10.dp)) }

            if (detail.year != null || detail.tracks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    ) {
                        detail.year?.let { year ->
                            MetaLine(label = "Año", value = year)
                        }
                        if (detail.tracks.isNotEmpty()) {
                            MetaLine(label = "Duración total", value = totalDuration)
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }

            when {
                detail.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Purple, strokeWidth = 2.dp)
                        }
                    }
                }
                detail.error != null -> {
                    item {
                        Text(
                            detail.error,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                detail.tracks.isEmpty() -> {
                    item {
                        Text(
                            "Sin listado de canciones para este álbum.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                else -> {
                    items(detail.tracks, key = { "${it.rank}-${it.name}" }) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = track.rank.toString().padStart(2, '0'),
                                fontSize = 11.sp,
                                color = TextMuted,
                                modifier = Modifier.width(26.dp),
                            )
                            Text(
                                text = track.name,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = track.duration,
                                fontSize = 12.sp,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(14.dp)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isFavorite) Color.White.copy(alpha = 0.10f)
                            else Purple.copy(alpha = 0.22f),
                        )
                        .clickable(onClick = onToggleFavorite)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isFavorite) "♥ En favoritas" else "♡ Agregar a favoritas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isFavorite) TextPrimary else Purple,
                    )
                }
            }


            item { Spacer(Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}
