package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.Album
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.UserTag
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.api.TrackInfoDetail
import com.jfcardenas.musicwall.data.CoverFallbackRepository
import com.jfcardenas.musicwall.data.CoverSearchState
import com.jfcardenas.musicwall.data.CoverUpdateBus
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao
import com.jfcardenas.musicwall.data.local.db.entity.AlbumEntity
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoverItem(
    val imageUrl: String,
    val albumName: String,
    val artistName: String,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    private val albumDao: AlbumDao,
    private val coverFallbackRepo: CoverFallbackRepository,
    private val coverBus: CoverUpdateBus,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    data class UiState(
        val username: String = "",
        val avatarUrl: String? = null,
        val isLoading: Boolean = false,
        val currentTrack: RecentTrack? = null,
        val isNowPlaying: Boolean = false,
        val recentTracks: List<RecentTrack> = emptyList(),
        val playcount: String = "—",
        val artistCount: String = "—",
        val weekAlbums: List<Album> = emptyList(),
        val recommendations: List<Album> = emptyList(),
        val topTags: List<UserTag> = emptyList(),
        // artist::album (lowercase) → URL override cuando Last.fm no tiene portada
        val coverOverrides: Map<String, String> = emptyMap(),
        val vsCoverA: CoverItem? = null,
        val vsCoverB: CoverItem? = null,
        val songDurationMs: Long = 0L,
        val error: String? = null,
        val currentTrackCoverUrl: String? = null,
        val coverSearchState: CoverSearchState = CoverSearchState.Idle,
    )

    var uiState by mutableStateOf(UiState())
        private set

    init {
        refresh()
        viewModelScope.launch {
            coverBus.updates.collect { update -> applyBusUpdate(update) }
        }
    }

    fun refresh() {
        val prefs = context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (username.isEmpty()) {
            uiState = UiState(error = "Sin cuenta conectada. Ve a Fuente para conectar.")
            return
        }
        uiState = uiState.copy(username = username, isLoading = true, error = null)
        viewModelScope.launch {
            try {
                coroutineScope {
                    val recentJob  = async { lastFmService.getRecentTracks(user = username, limit = 20) }
                    val infoJob    = async { lastFmService.getUserInfo(user = username) }
                    val weekJob    = async {
                        try { lastFmService.getTopAlbums(user = username, period = "7day", limit = 8).topAlbums.albums }
                        catch (_: Exception) { emptyList() }
                    }
                    val tagsJob    = async {
                        try { lastFmService.getUserTopTags(user = username, limit = 12).topTags.tags }
                        catch (_: Exception) { emptyList<UserTag>() }
                    }
                    val artistsJob = async {
                        try { lastFmService.getTopArtists(user = username, period = "1month", limit = 3).topArtists.artists }
                        catch (_: Exception) { emptyList() }
                    }
                    val vsDbJob    = async {
                        try { albumDao.getRandomWithImage(20) }
                        catch (_: Exception) { emptyList<AlbumEntity>() }
                    }

                    val recent     = recentJob.await()
                    val info       = infoJob.await()
                    val weekAlbums = weekJob.await()
                    val tags       = tagsJob.await()
                    val topArtists = artistsJob.await()
                    val vsFromDb   = vsDbJob.await()
                    val tracks     = recent.recentTracks.tracks
                    val track      = tracks.firstOrNull()

                    // Recomendaciones basadas en los artistas más escuchados del mes
                    val userAlbumKeys = weekAlbums.map { "${it.artist.name}::${it.name}".lowercase() }.toSet()
                    val recommendations = topArtists
                        .map { artist ->
                            async {
                                try {
                                    lastFmService.getArtistTopAlbums(artist = artist.name, limit = 4)
                                        .topAlbums.albums
                                        .filter { a ->
                                            "${a.artist.name}::${a.name}".lowercase() !in userAlbumKeys
                                                && a.images.getExtraLargeUrl() != null
                                        }
                                        .take(2)
                                } catch (_: Exception) { emptyList() }
                            }
                        }
                        .awaitAll()
                        .flatten()
                        .distinctBy { "${it.artist.name}::${it.name}".lowercase() }
                        .take(6)

                    // Overrides de portada desde cache para álbumes sin imagen de Last.fm
                    val allAlbums = weekAlbums + recommendations
                    val coverOverrides = allAlbums
                        .filter { it.images.getExtraLargeUrl() == null }
                        .mapNotNull { album ->
                            coverFallbackRepo.getCachedUrl(album.artist.name, album.name)
                                ?.let { url -> "${album.artist.name}::${album.name}".lowercase() to url }
                        }
                        .toMap()
                        .toMutableMap()

                    // Portada efectiva del track actual: Last.fm → cache
                    val lastFmCover = track?.images?.getExtraLargeUrl()
                    val albumName   = track?.album?.name?.takeIf { it.isNotBlank() }
                    val coverUrl    = lastFmCover
                        ?: albumName?.let { coverFallbackRepo.getCachedUrl(track!!.artist.name, it) }

                    val (vsA, vsB) = buildVsCovers(vsFromDb, weekAlbums)
                    val songDurationMs = track?.let { fetchDurationMs(it.artist.name, it.name) } ?: 0L

                    uiState = UiState(
                        username             = username,
                        avatarUrl            = info.user.images?.getExtraLargeUrl(),
                        currentTrack         = track,
                        isNowPlaying         = track?.attr?.nowplaying == "true",
                        recentTracks         = tracks,
                        playcount            = info.user.playcount,
                        artistCount          = info.user.artistCount ?: "—",
                        weekAlbums           = weekAlbums,
                        recommendations      = recommendations,
                        topTags              = tags,
                        coverOverrides       = coverOverrides,
                        vsCoverA             = vsA,
                        vsCoverB             = vsB,
                        songDurationMs       = songDurationMs,
                        isLoading            = false,
                        currentTrackCoverUrl = coverUrl,
                        coverSearchState     = CoverSearchState.Idle,
                    )
                }
            } catch (_: Exception) {
                uiState = uiState.copy(isLoading = false, error = "No se pudo cargar. Revisa tu conexión.")
            }
        }
    }

    /** El usuario pidió explícitamente buscar la portada en Deezer. */
    fun searchCurrentCover() {
        val track = uiState.currentTrack ?: return
        val albumName = track.album.name.takeIf { it.isNotBlank() } ?: return
        uiState = uiState.copy(coverSearchState = CoverSearchState.Searching)
        viewModelScope.launch {
            val url = coverFallbackRepo.findAndSave(track.artist.name, albumName)
            uiState = if (url != null) {
                uiState.copy(currentTrackCoverUrl = url, coverSearchState = CoverSearchState.Idle)
            } else {
                uiState.copy(coverSearchState = CoverSearchState.NotFound)
            }
        }
    }

    private fun applyBusUpdate(update: CoverUpdateBus.CoverUpdate) {
        // Actualizar portada del track actual si coincide
        val track = uiState.currentTrack
        if (uiState.currentTrackCoverUrl == null && track != null) {
            val trackKey = "${track.artist.name}::${track.album.name}".lowercase()
            if (trackKey == update.key) {
                uiState = uiState.copy(currentTrackCoverUrl = update.url)
                return
            }
        }
        // Actualizar coverOverrides para álbumes de la semana y recomendaciones
        val allAlbumKeys = (uiState.weekAlbums + uiState.recommendations)
            .map { "${it.artist.name}::${it.name}".lowercase() }
            .toSet()
        if (update.key in allAlbumKeys && update.key !in uiState.coverOverrides) {
            uiState = uiState.copy(
                coverOverrides = uiState.coverOverrides + (update.key to update.url)
            )
        }
    }

    private suspend fun fetchDurationMs(artist: String, track: String): Long {
        return try {
            lastFmService.getTrackInfo(artist = artist, track = track)
                .track?.duration?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun buildVsCovers(
        dbAlbums: List<AlbumEntity>,
        weekAlbums: List<Album>,
    ): Pair<CoverItem?, CoverItem?> {
        val fromDb = dbAlbums
            .filter { it.imageUrl.isNotEmpty() }
            .distinctBy { it.imageUrl }
            .shuffled()
        if (fromDb.size >= 2) {
            return Pair(
                CoverItem(fromDb[0].imageUrl, fromDb[0].albumName, fromDb[0].artistName),
                CoverItem(fromDb[1].imageUrl, fromDb[1].albumName, fromDb[1].artistName),
            )
        }
        val fromWeek = weekAlbums
            .mapNotNull { a -> a.images.getExtraLargeUrl()?.let { CoverItem(it, a.name, a.artist.name) } }
            .distinctBy { it.imageUrl }
            .shuffled()
        return Pair(fromWeek.getOrNull(0), fromWeek.getOrNull(1))
    }
}

fun relativeTime(uts: String?): String {
    val ts = uts?.toLongOrNull() ?: return "—"
    val diffSeconds = System.currentTimeMillis() / 1000 - ts
    return when {
        diffSeconds < 60      -> "hace un momento"
        diffSeconds < 3600    -> "hace ${diffSeconds / 60} min"
        diffSeconds < 86400   -> "hace ${diffSeconds / 3600} h"
        else                  -> "hace ${diffSeconds / 86400} días"
    }
}
