package com.jfcardenas.musicwall.features.wallpaper.renderer

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRendererFactory @Inject constructor(
    private val ecosystemRenderer: EcosystemRenderer
) {
    fun get(): WallpaperRenderer = ecosystemRenderer
}
