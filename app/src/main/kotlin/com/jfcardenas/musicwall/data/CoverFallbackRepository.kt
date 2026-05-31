package com.jfcardenas.musicwall.data

import android.content.Context
import com.jfcardenas.musicwall.api.DeezerService
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class CoverSearchState { Idle, Searching, NotFound }

@Singleton
class CoverFallbackRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deezerService: DeezerService,
    private val albumDao: AlbumDao,
    private val coverBus: CoverUpdateBus,
) {
    private val prefs get() =
        context.getSharedPreferences("cover_fallbacks", Context.MODE_PRIVATE)

    /** Devuelve la URL cacheada si ya se buscó antes, sin llamar a ninguna API. */
    fun getCachedUrl(artist: String, album: String): String? =
        prefs.getString(key(artist, album), null)

    /**
     * Busca en Deezer, persiste el resultado y lo devuelve.
     * Al encontrar una portada, actualiza Room y emite al bus para que
     * todos los ViewModels que observen se enteren sin necesidad de recargar.
     */
    suspend fun findAndSave(artist: String, album: String): String? {
        val url = try {
            deezerService.searchAlbum("$artist $album")
                .data?.firstOrNull()
                ?.let { it.coverXl ?: it.coverMedium }
        } catch (_: Exception) { null }

        if (url != null) {
            prefs.edit().putString(key(artist, album), url).apply()
            albumDao.updateImageUrlIfEmpty(album, artist, url)
            coverBus.emit(CoverUpdateBus.CoverUpdate(artist, album, url))
        }
        return url
    }

    private fun key(artist: String, album: String) =
        "fallback::${artist.trim().lowercase()}::${album.trim().lowercase()}"
}
