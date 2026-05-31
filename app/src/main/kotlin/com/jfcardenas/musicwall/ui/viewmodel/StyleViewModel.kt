package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.Album
import com.jfcardenas.musicwall.api.ArtistItem
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StyleViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    data class UiState(
        val topAlbums: List<Album> = emptyList(),
        val topArtists: List<ArtistItem> = emptyList(),
        val isLoading: Boolean = false,
        val hasAccount: Boolean = false,
    )

    var uiState by mutableStateOf(UiState())
        private set

    init { load() }

    fun load() {
        val prefs = context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (username.isEmpty()) return
        uiState = uiState.copy(isLoading = true, hasAccount = true)
        viewModelScope.launch {
            try {
                coroutineScope {
                    val albumsJob  = async { lastFmService.getTopAlbums(user = username, period = "1month", limit = 10) }
                    val artistsJob = async { lastFmService.getTopArtists(user = username, period = "overall", limit = 8) }
                    uiState = UiState(
                        topAlbums  = albumsJob.await().topAlbums.albums,
                        topArtists = artistsJob.await().topArtists.artists,
                        isLoading  = false,
                        hasAccount = true,
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}
