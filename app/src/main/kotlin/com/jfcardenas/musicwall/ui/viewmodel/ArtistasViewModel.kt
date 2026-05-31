package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.Album
import com.jfcardenas.musicwall.api.ArtistItem
import com.jfcardenas.musicwall.api.DeezerService
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.UserTag
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.CoverFallbackRepository
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistasViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    private val deezerService: DeezerService,
    private val coverFallbackRepo: CoverFallbackRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    data class UiState(
        val topAlbums: List<Album> = emptyList(),
        val topArtists: List<ArtistItem> = emptyList(),
        val artistImages: Map<String, String> = emptyMap(),
        val topTags: List<UserTag> = emptyList(),
        val recommendations: List<Album> = emptyList(),
        // key = "artist::album" (lowercase) → URL cacheada de Deezer
        val coverOverrides: Map<String, String> = emptyMap(),
        val isLoading: Boolean = false,
    )

    var uiState by mutableStateOf(UiState())
        private set

    init { load() }

    fun load() {
        val prefs = context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (username.isEmpty()) return
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            try {
                coroutineScope {
                    val albumsJob  = async { lastFmService.getTopAlbums(user = username, period = "1month", limit = 15) }
                    val artistsJob = async { lastFmService.getTopArtists(user = username, period = "1month", limit = 10) }
                    val tagsJob    = async {
                        try { lastFmService.getUserTopTags(user = username, limit = 15).topTags.tags }
                        catch (e: Exception) { emptyList() }
                    }

                    val albums  = albumsJob.await().topAlbums.albums
                    val artists = artistsJob.await().topArtists.artists
                    val tags    = tagsJob.await()

                    val deezerImages = artists
                        .map { artist ->
                            async {
                                val url = try {
                                    deezerService.searchArtist(artist.name)
                                        .data?.firstOrNull()?.pictureXl
                                } catch (e: Exception) { null }
                                artist.name to url
                            }
                        }
                        .awaitAll()
                        .filter { (_, url) -> !url.isNullOrBlank() }
                        .associate { (name, url) -> name to url!! }

                    // Recomendaciones: top álbumes de los 3 artistas más escuchados,
                    // excluyendo los que el usuario ya tiene en su top
                    val userAlbumKeys = albums.map { "${it.artist.name}::${it.name}".lowercase() }.toSet()
                    val recommendations = artists.take(3)
                        .map { artist ->
                            async {
                                try {
                                    lastFmService.getArtistTopAlbums(artist = artist.name, limit = 4)
                                        .topAlbums.albums
                                        .filter { a ->
                                            val key = "${a.artist.name}::${a.name}".lowercase()
                                            key !in userAlbumKeys && a.images.getExtraLargeUrl() != null
                                        }
                                        .take(2)
                                } catch (e: Exception) { emptyList() }
                            }
                        }
                        .awaitAll()
                        .flatten()
                        .distinctBy { "${it.artist.name}::${it.name}".lowercase() }
                        .take(8)

                    // Revisar cache de fallback para álbumes sin portada
                    val allAlbums = albums + recommendations
                    val coverOverrides = allAlbums
                        .filter { it.images.getExtraLargeUrl() == null }
                        .mapNotNull { album ->
                            coverFallbackRepo.getCachedUrl(album.artist.name, album.name)
                                ?.let { url -> "${album.artist.name}::${album.name}".lowercase() to url }
                        }
                        .toMap()

                    uiState = UiState(
                        topAlbums       = albums,
                        topArtists      = artists,
                        artistImages    = deezerImages,
                        topTags         = tags,
                        recommendations = recommendations,
                        coverOverrides  = coverOverrides,
                        isLoading       = false,
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}
