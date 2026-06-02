package com.jfcardenas.musicwall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jfcardenas.musicwall.api.formatTotalDuration
import com.jfcardenas.musicwall.ui.theme.*
import com.jfcardenas.musicwall.ui.viewmodel.AlbumDetailState

/** @deprecated Usar [AlbumTracksOverlay] */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailSheet(
    detail: AlbumDetailState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
) {
    val totalSeconds = detail.tracks.sumOf { it.durationSeconds }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = detail.cover.albumName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail.cover.artistName,
                fontSize = 14.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                detail.year?.let { MetaChip(it) }
                MetaChip(formatTotalDuration(totalSeconds))
            }
            Spacer(Modifier.height(12.dp))
            when {
                detail.isLoading -> {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Purple, strokeWidth = 2.dp)
                    }
                }
                detail.error != null -> Text(detail.error, color = TextSecondary)
                else -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(detail.tracks, key = { "${it.rank}-${it.name}" }) { track ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    track.rank.toString().padStart(2, '0'),
                                    modifier = Modifier.width(28.dp),
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                )
                                Text(
                                    track.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(track.duration, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onToggleFavorite) {
                Text(
                    if (isFavorite) "Quitar de favoritas" else "Agregar a favoritas",
                    color = Purple,
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Card)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
