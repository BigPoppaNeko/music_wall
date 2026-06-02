package com.jfcardenas.musicwall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.DiscogsService
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.LrcLibResult
import com.jfcardenas.musicwall.api.LrcLibService
import com.jfcardenas.musicwall.api.LyricsService
import com.jfcardenas.musicwall.api.MasterVersion
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.CoverFallbackRepository
import com.jfcardenas.musicwall.data.CoverSearchState
import com.jfcardenas.musicwall.data.FavoriteKeys
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao
import com.jfcardenas.musicwall.data.local.db.entity.FavoriteAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingDetailViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    private val lyricsService: LyricsService,
    private val lrcLibService: LrcLibService,
    private val discogsService: DiscogsService,
    private val coverFallbackRepo: CoverFallbackRepository,
    private val favoriteDao: FavoriteAlbumDao,
) : ViewModel() {

    enum class FormatType(val icon: String, val label: String) {
        VINYL("♦", "Vinilo"),
        CASSETTE("⬛", "Casete"),
        CD("●", "CD"),
        DIGITAL("⌁", "Digital"),
    }

    data class FormatGroup(val type: FormatType)

    data class FirstEdition(val year: String?, val country: String?, val label: String?)

    data class UiState(
        val isLoadingLyrics: Boolean = false,
        val isLoadingCuriosity: Boolean = false,
        val isLoadingVersions: Boolean = false,
        val albumImageUrl: String? = null,
        val coverReady: Boolean = false,
        val coverFallback: CoverSearchState = CoverSearchState.Idle,
        val isFavorite: Boolean = false,
        val lyrics: String? = null,
        val lyricsSource: String? = null,
        val curiosity: String? = null,
        val curiositySource: String? = null,
        val lyricsError: String? = null,
        val lyricsFallback: CoverSearchState = CoverSearchState.Idle,
        val curiosityError: String? = null,
        val formatGroups: List<FormatGroup> = emptyList(),
        val totalVersions: Int = 0,
        val firstEdition: FirstEdition? = null,
        val versions: List<MasterVersion> = emptyList(),
        val albumTitle: String? = null,
        val vinylThumb: String? = null,
        val cdThumb: String? = null,
        val cassetteThumb: String? = null,
    )

    var uiState by mutableStateOf(UiState())
        private set

    private var currentArtist = ""
    private var currentTrack  = ""
    private var currentAlbumHint = ""

    fun load(artist: String, track: String, albumHint: String = "") {
        currentArtist = artist
        currentTrack  = track
        currentAlbumHint = albumHint.trim()
        uiState = UiState(isLoadingLyrics = true, isLoadingCuriosity = true, isLoadingVersions = true)
        viewModelScope.launch { fetchLyrics(artist, track) }
        viewModelScope.launch { fetchInfoAndVersions(artist, track) }
    }

    // ── Fallback de portada (Deezer) ──────────────────────────────────────────

    fun fetchCoverFallback() {
        val albumTitle = uiState.albumTitle ?: currentTrack
        uiState = uiState.copy(coverFallback = CoverSearchState.Searching)
        viewModelScope.launch {
            val url = coverFallbackRepo.findAndSave(currentArtist, albumTitle)
            uiState = if (url != null) {
                uiState.copy(albumImageUrl = url, coverFallback = CoverSearchState.Idle)
            } else {
                uiState.copy(coverFallback = CoverSearchState.NotFound)
            }
        }
    }

    // ── Favoritas ─────────────────────────────────────────────────────────────

    fun toggleFavorite() {
        val albumTitle = uiState.albumTitle ?: return
        val id = favoriteId(currentArtist, albumTitle)
        viewModelScope.launch {
            if (uiState.isFavorite) {
                favoriteDao.delete(id)
                uiState = uiState.copy(isFavorite = false)
            } else {
                favoriteDao.insert(FavoriteAlbum(
                    id         = id,
                    albumName  = albumTitle,
                    artistName = currentArtist,
                    imageUrl   = uiState.albumImageUrl,
                ))
                uiState = uiState.copy(isFavorite = true)
            }
        }
    }

    private suspend fun checkFavorite(albumTitle: String) {
        val fav = favoriteDao.isFavorite(favoriteId(currentArtist, albumTitle))
        uiState = uiState.copy(isFavorite = fav)
    }

    fun applyCoverSwap(cover: CoverItem) {
        uiState = uiState.copy(
            albumImageUrl = cover.imageUrl,
            albumTitle = cover.albumName,
        )
    }

    fun fetchLyricsFallback() {
        uiState = uiState.copy(lyricsFallback = CoverSearchState.Searching, lyricsError = null)
        viewModelScope.launch {
            val lyrics = tryFetchLrcLib(currentArtist, currentTrack)
                ?: tryFetchLrcLib(currentArtist, cleanTitle(currentTrack))
                ?: tryFetchLrcLibQuery(currentArtist, currentTrack)
            uiState = if (lyrics != null) {
                uiState.copy(
                    lyrics = lyrics,
                    lyricsSource = "LRCLib",
                    lyricsFallback = CoverSearchState.Idle,
                    lyricsError = null,
                    isLoadingLyrics = false,
                )
            } else {
                uiState.copy(
                    lyricsFallback = CoverSearchState.NotFound,
                    lyricsError = "Letra no encontrada en otras fuentes",
                )
            }
        }
    }

    // ── Letras ────────────────────────────────────────────────────────────────

    private suspend fun fetchLyrics(artist: String, track: String) {
        val lyrics = tryFetchLyrics(artist, track)
            ?: tryFetchLyrics(artist, cleanTitle(track))
        uiState = if (lyrics != null) {
            uiState.copy(
                isLoadingLyrics = false,
                lyrics = lyrics,
                lyricsSource = "lyrics.ovh",
                lyricsFallback = CoverSearchState.Idle,
            )
        } else {
            uiState.copy(
                isLoadingLyrics = false,
                lyricsError = "Letra no encontrada",
                lyricsFallback = CoverSearchState.Idle,
            )
        }
    }

    private suspend fun tryFetchLyrics(artist: String, track: String): String? =
        try {
            lyricsService.getLyrics(artist = artist, title = track)
                .lyrics?.trim()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }

    private suspend fun tryFetchLrcLib(artist: String, track: String): String? =
        try {
            lrcLibService.search(trackName = track, artistName = artist)
                .firstOrNull()
                ?.let { pickLrcLyrics(it) }
        } catch (_: Exception) {
            null
        }

    private suspend fun tryFetchLrcLibQuery(artist: String, track: String): String? =
        try {
            lrcLibService.searchQuery("$artist $track")
                .firstOrNull()
                ?.let { pickLrcLyrics(it) }
        } catch (_: Exception) {
            null
        }

    private fun pickLrcLyrics(result: LrcLibResult): String? =
        result.plainLyrics?.trim()?.takeIf { it.isNotBlank() }
            ?: result.syncedLyrics
                ?.lineSequence()
                ?.mapNotNull { line ->
                    Regex("\\]\\s*(.+)$").find(line)?.groupValues?.getOrNull(1)?.trim()
                }
                ?.joinToString("\n")
                ?.trim()
                ?.takeIf { it.isNotBlank() }

    private fun cleanTitle(title: String): String =
        title
            .replace(Regex("\\s*[\\(\\[][^)\\]]*[\\)\\]]"), "")
            .replace(Regex("\\s*-?\\s*feat\\..*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*ft\\..*", RegexOption.IGNORE_CASE), "")
            .trim()

    // ── Info + versiones ──────────────────────────────────────────────────────

    private suspend fun fetchInfoAndVersions(artist: String, track: String) {
        try {
            val trackInfo = try {
                lastFmService.getTrackInfo(artist = artist, track = track)
            } catch (e: Exception) { null }

            val lastFmAlbumTitle = trackInfo?.track?.album?.title
            val shouldTrustLastFmAlbum = currentAlbumHint.isBlank() ||
                currentAlbumHint.equals(lastFmAlbumTitle, ignoreCase = true)
            val lastFmUrl = if (shouldTrustLastFmAlbum) {
                trackInfo?.track?.album?.let { album ->
                    album.images?.getExtraLargeUrl()?.takeIf { it.isNotBlank() }
                        ?: album.mbid?.takeIf { it.isNotBlank() }
                            ?.let { "https://coverartarchive.org/release/$it/front-250" }
                }
            } else {
                null
            }
            // Si Last.fm no tiene portada, intentar con el cache persistido
            val albumNameForLookup = preferredAlbumTitle(lastFmAlbumTitle)
            val cachedUrl = if (lastFmUrl == null && !albumNameForLookup.isNullOrBlank()) {
                coverFallbackRepo.getCachedUrl(currentArtist, albumNameForLookup)
            } else null
            val imageUrl = lastFmUrl ?: cachedUrl
            uiState = uiState.copy(albumImageUrl = imageUrl, coverReady = true)

            val trackWiki = trackInfo?.track?.wiki?.summary?.stripHtml()?.takeIf { it.isNotBlank() }
            if (trackWiki != null) {
                uiState = uiState.copy(
                    isLoadingCuriosity = false,
                    curiosity = trackWiki,
                    curiositySource = "canción",
                )
            } else {
                val artistInfo = try {
                    lastFmService.getArtistInfo(artist = artist)
                } catch (e: Exception) { null }
                val artistBio = artistInfo?.artist?.bio?.summary?.stripHtml()?.takeIf { it.isNotBlank() }
                uiState = if (artistBio != null) {
                    uiState.copy(isLoadingCuriosity = false, curiosity = artistBio, curiositySource = "artista")
                } else {
                    uiState.copy(isLoadingCuriosity = false, curiosityError = "Sin información disponible")
                }
            }

            val albumTitle = preferredAlbumTitle(lastFmAlbumTitle)
            if (!albumTitle.isNullOrBlank()) {
                uiState = uiState.copy(albumTitle = albumTitle)
                checkFavorite(albumTitle)
                fetchDiscogsVersions(artist, albumTitle)
            } else {
                uiState = uiState.copy(isLoadingVersions = false)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(
                coverReady = true,
                isLoadingCuriosity = false,
                isLoadingVersions = false,
                curiosityError = "No se pudo cargar",
            )
        }
    }

    private suspend fun fetchDiscogsVersions(artist: String, albumTitle: String) {
        try {
            val masterId = discogsService.searchMaster(
                releaseTitle = albumTitle,
                artist = artist,
            ).results?.firstOrNull()?.id

            if (masterId == null) {
                uiState = uiState.copy(isLoadingVersions = false)
                return
            }

            val response = discogsService.getMasterVersions(masterId = masterId)
            val versions = response.versions ?: emptyList()
            val totalItems = response.pagination?.items ?: 0

            val groups = versions
                .mapNotNull { parseFormatType(it.format) }
                .toSet()
                .sortedBy { it.ordinal }
                .map { FormatGroup(it) }

            val firstEd = versions
                .filter { parseFormatType(it.format) in listOf(FormatType.VINYL, FormatType.CASSETTE, FormatType.CD) }
                .filter { it.released?.toIntOrNull() != null }
                .minByOrNull { it.released?.toIntOrNull() ?: Int.MAX_VALUE }
                ?.let { FirstEdition(year = it.released, country = it.country, label = it.label) }

            fun isValidThumb(t: String?) = !t.isNullOrBlank() && t != "https://st.discogs.com/"
            val vinylThumb    = versions.firstOrNull { parseFormatType(it.format) == FormatType.VINYL    && isValidThumb(it.thumb) }?.thumb
            val cdThumb       = versions.firstOrNull { parseFormatType(it.format) == FormatType.CD       && isValidThumb(it.thumb) }?.thumb
            val cassetteThumb = versions.firstOrNull { parseFormatType(it.format) == FormatType.CASSETTE && isValidThumb(it.thumb) }?.thumb

            uiState = uiState.copy(
                isLoadingVersions = false,
                formatGroups      = groups,
                totalVersions     = totalItems,
                firstEdition      = firstEd,
                versions          = versions,
                albumTitle        = albumTitle,
                vinylThumb        = vinylThumb,
                cdThumb           = cdThumb,
                cassetteThumb     = cassetteThumb,
            )
        } catch (e: Exception) {
            uiState = uiState.copy(isLoadingVersions = false)
        }
    }

    private fun preferredAlbumTitle(lastFmAlbum: String?): String? =
        currentAlbumHint.takeIf { it.isNotBlank() }
            ?: lastFmAlbum?.takeIf { it.isNotBlank() }

    private fun parseFormatType(format: String?): FormatType? {
        if (format == null) return null
        return when {
            format.contains("LP")
                || format.contains("7\"")
                || format.contains("10\"")
                || format.contains("12\"")
                || format.contains("Vinyl", ignoreCase = true)  -> FormatType.VINYL
            format.contains("Cass", ignoreCase = true)
                || format.startsWith("MC,")
                || format == "MC"                               -> FormatType.CASSETTE
            format.contains("CD") || format.contains("SACD")   -> FormatType.CD
            format.contains("File", ignoreCase = true)
                || format.contains("Digital", ignoreCase = true) -> FormatType.DIGITAL
            else -> null
        }
    }

    private fun favoriteId(artist: String, album: String) = FavoriteKeys.id(artist, album)
}

private fun String.stripHtml(): String =
    replace(Regex("<[^>]+>"), "")
        .replace("Read more on Last.fm.", "")
        .replace(Regex("\\s+"), " ")
        .trim()
