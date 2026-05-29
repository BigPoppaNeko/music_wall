package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.Bitmap
import com.jfcardenas.musicwall.domain.model.MusicImage

data class RenderItem(val bitmap: Bitmap, val image: MusicImage)

interface WallpaperRenderer {
    val id: String
    val isPremium: Boolean
    suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap
    fun release() {}
}
