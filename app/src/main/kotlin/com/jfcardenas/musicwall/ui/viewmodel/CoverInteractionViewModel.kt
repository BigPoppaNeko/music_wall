package com.jfcardenas.musicwall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.AlbumInfoDetail
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.formatTrackDuration
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.api.releaseYear
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao
import com.jfcardenas.musicwall.data.local.db.entity.FavoriteAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

fun pickAlternateCover(current: CoverItem, pool: List<CoverItem>): CoverItem? =
    pool.asSequence()
        .filter { it.imageUrl.isNotBlank() && it.key != current.key }
        .distinctBy { it.key }
        .shuffled()
        .firstOrNull()

data class AlbumTrackRow(
    val rank: Int,
    val name: String,
    val duration: String,
    val durationSeconds: Long = 0L,
)

data class AlbumDetailState(
    val cover: CoverItem,
    val album: AlbumInfoDetail? = null,
    val tracks: List<AlbumTrackRow> = emptyList(),
    val year: String? = null,
    val tags: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class CoverInteractionViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    private val favoriteDao: FavoriteAlbumDao,
) : ViewModel() {

    var favoriteKeys by mutableStateOf<Set<String>>(emptySet())
        private set

    var selectedDetail by mutableStateOf<AlbumDetailState?>(null)
        private set

    var tracksOverlay by mutableStateOf<AlbumDetailState?>(null)
        private set

    private var tracksOverlayVetoHandler: ((CoverItem) -> Unit)? = null
    private var tracksOverlayFavoritedHandler: ((CoverItem) -> Unit)? = null
    private var overlayLoadGeneration = 0
    var tracksOverlayShowVeto by mutableStateOf(false)
        private set

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    init {
        refreshFavorites()
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            favoriteKeys = favoriteDao.getAll().map { it.id }.toSet()
        }
    }

    fun isFavorite(key: String): Boolean = key in favoriteKeys

    fun toggleFavorite(cover: CoverItem) {
        viewModelScope.launch {
            if (cover.key in favoriteKeys) {
                favoriteDao.delete(cover.key)
                favoriteKeys = favoriteKeys - cover.key
                snackbarMessage = "Quitada de favoritas"
            } else {
                favoriteDao.insert(
                    FavoriteAlbum(
                        id = cover.key,
                        albumName = cover.albumName,
                        artistName = cover.artistName,
                        imageUrl = cover.imageUrl,
                    ),
                )
                favoriteKeys = favoriteKeys + cover.key
                snackbarMessage = "Agregada a favoritas"
            }
        }
    }

    fun clearSnackbar() {
        snackbarMessage = null
    }

    fun openAlbumDetail(cover: CoverItem) {
        openCoverOverlay(cover)
    }

    fun openCoverOverlay(
        cover: CoverItem,
        showVeto: Boolean = false,
        onVeto: ((CoverItem) -> Unit)? = null,
        onFavorited: ((CoverItem) -> Unit)? = null,
    ) {
        val generation = ++overlayLoadGeneration
        tracksOverlayShowVeto = showVeto
        tracksOverlayVetoHandler = onVeto
        tracksOverlayFavoritedHandler = onFavorited
        loadAlbumDetail(cover, generation) { state ->
            if (generation == overlayLoadGeneration) {
                tracksOverlay = state
            }
        }
    }

    fun openTracksOverlay(cover: CoverItem, onVeto: (CoverItem) -> Unit) {
        openCoverOverlay(cover, showVeto = true, onVeto = onVeto)
    }

    fun closeTracksOverlay() {
        overlayLoadGeneration++
        tracksOverlay = null
        tracksOverlayVetoHandler = null
        tracksOverlayFavoritedHandler = null
        tracksOverlayShowVeto = false
    }

    fun vetoFromTracksOverlay() {
        val cover = tracksOverlay?.cover ?: return
        tracksOverlayVetoHandler?.invoke(cover)
        closeTracksOverlay()
    }

    fun toggleFavoriteFromOverlay() {
        val cover = tracksOverlay?.cover ?: return
        val wasFavorite = cover.key in favoriteKeys
        viewModelScope.launch {
            if (wasFavorite) {
                favoriteDao.delete(cover.key)
                favoriteKeys = favoriteKeys - cover.key
                snackbarMessage = "Quitada de favoritas"
            } else {
                favoriteDao.insert(
                    FavoriteAlbum(
                        id = cover.key,
                        albumName = cover.albumName,
                        artistName = cover.artistName,
                        imageUrl = cover.imageUrl,
                    ),
                )
                favoriteKeys = favoriteKeys + cover.key
                snackbarMessage = "Agregada a favoritas"
                tracksOverlayFavoritedHandler?.invoke(cover)
            }
        }
    }

    private fun loadAlbumDetail(cover: CoverItem, generation: Int, onLoaded: (AlbumDetailState) -> Unit) {
        onLoaded(AlbumDetailState(cover = cover, isLoading = true))
        viewModelScope.launch {
            try {
                val info = lastFmService.getAlbumInfo(
                    artist = cover.artistName,
                    album = cover.albumName,
                ).album
                if (generation != overlayLoadGeneration) return@launch
                if (info == null) {
                    onLoaded(
                        AlbumDetailState(
                            cover = cover,
                            isLoading = false,
                            error = "No encontramos este álbum en Last.fm",
                        ),
                    )
                    return@launch
                }
                val tracks = info.tracks?.tracks.orEmpty()
                    .sortedBy { it.attr?.rank?.toIntOrNull() ?: Int.MAX_VALUE }
                    .mapIndexed { index, track ->
                        AlbumTrackRow(
                            rank = track.attr?.rank?.toIntOrNull() ?: (index + 1),
                            name = track.name,
                            duration = formatTrackDuration(track.duration),
                            durationSeconds = track.duration?.toLongOrNull()?.coerceAtLeast(0L) ?: 0L,
                        )
                    }
                if (generation != overlayLoadGeneration) return@launch
                onLoaded(
                    AlbumDetailState(
                        cover = cover.copy(
                            imageUrl = info.images?.getExtraLargeUrl() ?: cover.imageUrl,
                        ),
                        album = info,
                        tracks = tracks,
                        year = info.releaseYear(),
                        tags = info.tags?.tags.orEmpty().map { it.name }.take(6),
                        isLoading = false,
                    ),
                )
            } catch (_: Exception) {
                if (generation != overlayLoadGeneration) return@launch
                onLoaded(
                    AlbumDetailState(
                        cover = cover,
                        isLoading = false,
                        error = "No pudimos cargar el detalle del álbum",
                    ),
                )
            }
        }
    }

    fun closeAlbumDetail() {
        selectedDetail = null
    }

    fun cycleCover(
        current: CoverItem,
        pool: List<CoverItem> = emptyList(),
        onReplaced: (CoverItem) -> Unit,
    ) {
        pickAlternateCover(current, pool)?.let {
            onReplaced(it)
            snackbarMessage = "Otra portada"
            return
        }
        viewModelScope.launch {
            fetchAlternativeCover(current)?.let {
                onReplaced(it)
                snackbarMessage = "Otra portada"
            }
        }
    }

    fun cycleDetailCover(pool: List<CoverItem> = emptyList()) {
        val current = selectedDetail?.cover ?: return
        cycleCover(current, pool) { newCover -> openAlbumDetail(newCover) }
    }

    private suspend fun fetchAlternativeCover(current: CoverItem): CoverItem? = try {
        lastFmService.getArtistTopAlbums(artist = current.artistName, limit = 40)
            .topAlbums.albums
            .mapNotNull { album ->
                album.images.getExtraLargeUrl()?.let { url ->
                    CoverItem(
                        imageUrl = url,
                        albumName = album.name,
                        artistName = album.artist.name,
                    )
                }
            }
            .firstOrNull { it.key != current.key }
    } catch (_: Exception) {
        null
    }
}
