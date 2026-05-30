package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.content.Context
import com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRendererFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streetRenderer:    StreetPosterRenderer,
    private val ecosystemRenderer: EcosystemRenderer,
    private val albumRenderer:     AlbumWallRenderer,
    private val cinematicRenderer: CinematicWallRenderer,
    private val physicalRenderer:  PhysicalCollageRenderer,
) {
    companion object {
        const val PREFS_NAME    = "music_wall_prefs"
        const val PREF_RENDERER = "renderer_id"
        const val DEFAULT       = "street"
    }

    private val all: Map<String, WallpaperRenderer> = mapOf(
        "street"    to streetRenderer,
        "ecosystem" to ecosystemRenderer,
        "album"     to albumRenderer,
        "cinematic" to cinematicRenderer,
        "physical"  to physicalRenderer,
    )

    /** Returns the renderer currently selected in SharedPreferences (defaults to street). */
    fun get(): WallpaperRenderer {
        val id = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(PREF_RENDERER, DEFAULT) ?: DEFAULT
        return all[id] ?: streetRenderer
    }

    fun all(): Map<String, WallpaperRenderer> = all

    fun select(id: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(PREF_RENDERER, id).apply()
    }

    // ── Organic module — no modifica renderers existentes ─────────────────────

    private val organicCache = mutableMapOf<String, OrganicRenderer>()

    /**
     * Devuelve (cacheando) el OrganicRenderer para el layoutId dado.
     * layoutId = nombre del archivo sin extensión dentro de assets/organic/
     */
    fun getOrganic(layoutId: String): OrganicRenderer =
        organicCache.getOrPut(layoutId) { OrganicRenderer(context, layoutId) }

    /**
     * Descubre todos los layouts orgánicos disponibles en assets/organic/.
     * Devuelve lista de layoutId (nombre sin extensión).
     */
    fun discoverOrganicLayouts(): List<String> = try {
        context.assets.list("organic")
            ?.filter { it.endsWith(".json") }
            ?.map { it.removeSuffix(".json") }
            ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}
