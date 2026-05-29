package com.jfcardenas.musicwall.features.wallpaper.renderer

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRendererFactory @Inject constructor(
    private val albumWallRenderer: AlbumWallRenderer
) {
    fun get(): WallpaperRenderer = albumWallRenderer
}
