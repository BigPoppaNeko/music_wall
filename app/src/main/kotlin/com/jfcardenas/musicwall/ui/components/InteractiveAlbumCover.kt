package com.jfcardenas.musicwall.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InteractiveAlbumCover(
    imageUrl: String?,
    contentDescription: String?,
    isFavorite: Boolean, // reservado para futura indicación estática; sin animaciones
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    onShowDetail: () -> Unit,
    onDoubleTap: () -> Unit = {},
    onTripleTap: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .coverMultiTapGestures(
                onSingleTap = onClick,
                onDoubleTap = onDoubleTap,
                onTripleTap = onTripleTap,
                onLongPress = onShowDetail,
            ),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private const val MultiTapWindowMs = 320L

private fun Modifier.coverMultiTapGestures(
    onSingleTap: (() -> Unit)?,
    onDoubleTap: () -> Unit,
    onTripleTap: (() -> Unit)?,
    onLongPress: () -> Unit,
): Modifier = pointerInput(onSingleTap, onDoubleTap, onTripleTap) {
    coroutineScope {
        var tapCount = 0
        var resetJob: Job? = null

        detectTapGestures(
            onLongPress = { onLongPress() },
            onTap = {
                tapCount++
                resetJob?.cancel()
                resetJob = launch {
                    delay(MultiTapWindowMs)
                    when {
                        tapCount >= 3 -> onTripleTap?.invoke()
                        tapCount == 2 -> onDoubleTap()
                        tapCount == 1 -> onSingleTap?.invoke()
                    }
                    tapCount = 0
                }
            },
        )
    }
}
