package com.jfcardenas.musicwall.data

import android.content.Context
import coil.size.Size
import kotlin.math.max
import kotlin.math.min

/** Tamaños Coil acotados para evitar OOM con portadas extralarge de Last.fm. */
object CoverImageSizing {

    private const val COLLAGE_TILE_MAX_PX = 960
    private const val COLLAGE_TILE_MIN_PX = 400

    /** Portadas individuales dentro de un mural / collage. */
    fun collageTileSize(context: Context): Size {
        val dm = context.resources.displayMetrics
        val halfScreen = max(dm.widthPixels, dm.heightPixels) / 2
        val side = min(max(halfScreen, COLLAGE_TILE_MIN_PX), COLLAGE_TILE_MAX_PX)
        return Size(side, side)
    }

    /** Wallpaper estático de una sola imagen a pantalla completa. */
    fun fullWallpaperSize(context: Context): Size {
        val dm = context.resources.displayMetrics
        return Size(dm.widthPixels, dm.heightPixels)
    }
}
