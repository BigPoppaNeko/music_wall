package com.jfcardenas.musicwall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    private val settings: UserSettingsRepository,
) : ViewModel() {

    data class SearchResult(
        val name: String,
        val subtitle: String,
        val imageUrl: String?,
    )

    sealed class SearchState {
        object Idle    : SearchState()
        object Loading : SearchState()
        data class Results(val items: List<SearchResult>) : SearchState()
        object Empty   : SearchState()
        object Error   : SearchState()
    }

    var artistState by mutableStateOf<SearchState>(SearchState.Idle)
        private set
    var albumState  by mutableStateOf<SearchState>(SearchState.Idle)
        private set

    private var searchJob: Job? = null

    fun search(query: String, tab: Int) {
        searchJob?.cancel()
        if (query.isBlank()) {
            artistState = SearchState.Idle
            albumState  = SearchState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            when (tab) {
                0 -> {
                    artistState = SearchState.Loading
                    artistState = try {
                        val artists = lastFmService.searchArtists(artist = query)
                            .results.artistMatches.artists
                            .map { SearchResult(it.name, formatListeners(it.listeners), it.images?.getExtraLargeUrl()) }
                        if (artists.isEmpty()) SearchState.Empty else SearchState.Results(artists)
                    } catch (_: Exception) { SearchState.Error }
                }
                1 -> {
                    albumState = SearchState.Loading
                    albumState = try {
                        val albums = lastFmService.searchAlbums(album = query)
                            .results.albumMatches.albums
                            .map { SearchResult(it.name, it.artist, it.images?.getExtraLargeUrl()) }
                        if (albums.isEmpty()) SearchState.Empty else SearchState.Results(albums)
                    } catch (_: Exception) { SearchState.Error }
                }
            }
        }
    }

    private fun formatListeners(raw: String?): String {
        val n = raw?.toLongOrNull() ?: return ""
        return when {
            n >= 1_000_000 -> "${n / 1_000_000}M oyentes"
            n >= 1_000     -> "${n / 1_000}K oyentes"
            else           -> "$n oyentes"
        }
    }

    fun saveSelectedArtists(artists: Collection<String>) {
        settings.saveExploreArtists(artists)
    }
}
