package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.content.Context
import com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WallpaperRendererFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mosaicRenderer:        MosaicBlendRenderer,
    private val puzzleRenderer:        PuzzleRenderer,
    private val psychedelicRenderer:   PsychedelicGridRenderer,
    private val manchesterRenderer:    ManchesterWallRenderer,
    @Named("scene_britrock")    private val britrockScene:    OrganicRenderer,
    @Named("scene_bano_bar_lima") private val banoBarLimaScene: OrganicRenderer,
    @Named("scene_woodstock")     private val woodstockScene:    OrganicRenderer,
) {
    companion object {
        const val PREFS_NAME    = "music_wall_prefs"
        const val PREF_RENDERER = "renderer_id"
        const val DEFAULT       = "mosaic"
    }

    private val all: Map<String, WallpaperRenderer> = mapOf(
        "scene_manchester"    to manchesterRenderer,
        "scene_britrock"      to britrockScene,
        "scene_bano_bar_lima" to banoBarLimaScene,
        "scene_woodstock"     to woodstockScene,
        "mosaic"              to mosaicRenderer,
        "puzzle"           to puzzleRenderer,
        "psychedelic"      to psychedelicRenderer,
    )

    fun get(): WallpaperRenderer {
        val id = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(PREF_RENDERER, DEFAULT) ?: DEFAULT
        return all[id] ?: mosaicRenderer
    }

    fun all(): Map<String, WallpaperRenderer> = all

    fun select(id: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(PREF_RENDERER, id).apply()
    }
}
