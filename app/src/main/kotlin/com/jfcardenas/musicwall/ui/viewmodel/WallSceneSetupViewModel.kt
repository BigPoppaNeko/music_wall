package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.SearchAlbum
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao
import com.jfcardenas.musicwall.domain.model.MusicImage
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WallSlotSource {
    MANUAL,
    RECENT_FAVORITES,
    WEEK_TOP,
}

data class WallSlot(
    val index: Int,
    val image: MusicImage? = null,
)

@HiltViewModel
class WallSceneSetupViewModel @Inject constructor(
    private val favoriteDao: FavoriteAlbumDao,
    private val lastFmService: LastFmService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    var selectedSource by mutableStateOf(WallSlotSource.RECENT_FAVORITES)
        private set

    var slots by mutableStateOf(emptySlots())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var selectedSlotIndex by mutableStateOf(1)
        private set

    var albumSearchQuery by mutableStateOf("")
        private set

    var albumSearchResults by mutableStateOf<List<MusicImage>>(emptyList())
        private set

    var isSearching by mutableStateOf(false)
        private set

    private val prefs = context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
    private var searchJob: Job? = null

    init {
        selectSource(WallSlotSource.RECENT_FAVORITES)
    }

    fun selectSource(source: WallSlotSource) {
        selectedSource = source
        error = null
        when (source) {
            WallSlotSource.MANUAL -> slots = emptySlots()
            WallSlotSource.RECENT_FAVORITES -> loadFavorites()
            WallSlotSource.WEEK_TOP -> loadWeeklyTop()
        }
    }

    fun selectSlot(index: Int) {
        selectedSlotIndex = index.coerceIn(1, SLOT_COUNT)
    }

    fun searchAlbums(query: String) {
        albumSearchQuery = query
        searchJob?.cancel()
        if (query.isBlank()) {
            albumSearchResults = emptyList()
            isSearching = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            isSearching = true
            albumSearchResults = try {
                lastFmService.searchAlbums(album = query, limit = 12)
                    .results.albumMatches.albums
                    .mapNotNull { it.toMusicImage() }
            } catch (_: Exception) {
                emptyList()
            }
            isSearching = false
        }
    }

    fun placeAlbum(image: MusicImage, slotIndex: Int = selectedSlotIndex) {
        slots = slots.map { slot ->
            if (slot.index == slotIndex) slot.copy(image = image) else slot
        }
        error = null
    }

    fun replaceSlotWithRandom(slotIndex: Int = selectedSlotIndex) {
        val pool = (albumSearchResults + slots.mapNotNull { it.image }).distinctBy { "${it.artistName}::${it.name}" }
        if (pool.isEmpty()) return
        val current = slots.find { it.index == slotIndex }?.image
        val replacement = pool.filter { it.url != current?.url && it.name != current?.name }.randomOrNull()
            ?: pool.random()
        placeAlbum(replacement, slotIndex)
    }

    fun fillRandomAllowRepeats() {
        val pool = (slots.mapNotNull { it.image } + albumSearchResults).ifEmpty { return }
        slots = slots.map { slot -> slot.copy(image = pool.random()) }
        error = null
    }

    fun persistSelection(): Boolean {
        val selected = slots.mapNotNull { it.image }
        if (selected.size < MIN_READY_SLOTS) {
            error = "Elige al menos $MIN_READY_SLOTS portadas para generar la escena."
            return false
        }
        val encoded = selected.joinToString("||") { image ->
            listOf(
                image.url.escapePart(),
                image.name.escapePart(),
                image.artistName.escapePart(),
                image.mbid.orEmpty().escapePart(),
                image.lastFmUrl.orEmpty().escapePart(),
            ).joinToString("|")
        }
        prefs.edit()
            .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_SELECTED_SLOTS)
            .putString(CollageWallpaper.PREF_SELECTED_SLOT_IMAGES, encoded)
            .putString(CollageWallpaper.PREF_IMAGE_KIND, "ALBUMS")
            .putString(CollageWallpaper.PREF_PERIOD, "random")
            .putString(CollageWallpaper.PREF_LIMIT, selected.size.toString())
            .apply()
        return true
    }

    private fun loadFavorites() {
        isLoading = true
        viewModelScope.launch {
            val images = try {
                favoriteDao.getAll()
                    .mapNotNull { favorite ->
                        favorite.imageUrl?.let {
                            MusicImage(
                                url = it,
                                name = favorite.albumName,
                                artistName = favorite.artistName,
                                rank = 0,
                                kind = MusicImage.Kind.ALBUM,
                                mbid = favorite.mbid,
                            )
                        }
                    }
                    .take(SLOT_COUNT)
            } catch (_: Exception) {
                emptyList()
            }
            slots = images.toSlots()
            error = if (images.isEmpty()) "Aún no tienes favoritas con portada." else null
            isLoading = false
        }
    }

    private fun loadWeeklyTop() {
        val username = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (username.isBlank()) {
            slots = emptySlots()
            error = "Conecta tu usuario de Last.fm para usar tu top semanal."
            return
        }
        isLoading = true
        viewModelScope.launch {
            val images = try {
                lastFmService.getTopAlbums(user = username, period = "7day", limit = SLOT_COUNT)
                    .topAlbums.albums
                    .mapIndexedNotNull { index, album ->
                        album.images.getExtraLargeUrl()?.let {
                            MusicImage(
                                url = it,
                                name = album.name,
                                artistName = album.artist.name,
                                rank = index + 1,
                                kind = MusicImage.Kind.ALBUM,
                                playcount = album.playcount?.toIntOrNull() ?: 0,
                                mbid = album.mbid?.takeIf { value -> value.isNotEmpty() },
                                lastFmUrl = album.url?.takeIf { value -> value.isNotEmpty() },
                            )
                        }
                    }
            } catch (_: Exception) {
                emptyList()
            }
            slots = images.toSlots()
            error = if (images.isEmpty()) "No encontramos portadas en tu top semanal." else null
            isLoading = false
        }
    }

    private fun List<MusicImage>.toSlots(): List<WallSlot> =
        (0 until SLOT_COUNT).map { index -> WallSlot(index = index + 1, image = getOrNull(index)) }

    private fun emptySlots(): List<WallSlot> =
        (1..SLOT_COUNT).map { WallSlot(index = it) }

    private fun String.escapePart(): String =
        replace("\\", "\\\\").replace("|", "\\p").replace("\n", "\\n")

    private fun SearchAlbum.toMusicImage(): MusicImage? {
        val imageUrl = images?.getExtraLargeUrl() ?: return null
        return MusicImage(
            url = imageUrl,
            name = name,
            artistName = artist,
            rank = 0,
            kind = MusicImage.Kind.ALBUM,
        )
    }

    companion object {
        const val SLOT_COUNT = 12
        const val MIN_READY_SLOTS = 4
    }
}
