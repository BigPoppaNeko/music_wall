package com.jfcardenas.musicwall.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jfcardenas.musicwall.ui.theme.Purple

@Composable
fun FavoriteToggleButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Purple,
    iconSize: Dp = 22.dp,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        FavoriteToggleIcon(isFavorite = isFavorite, tint = tint, size = iconSize)
    }
}

@Composable
fun FavoriteToggleIcon(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Purple,
    size: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
) {
    val iconModifier = modifier
        .size(size)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Icon(
        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
        contentDescription = if (isFavorite) "Quitar de favoritas" else "Agregar a favoritas",
        tint = tint,
        modifier = iconModifier,
    )
}
