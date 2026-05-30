package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.RecentTrack
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    data class UiState(
        val username: String = "",
        val avatarUrl: String? = null,
        val isLoading: Boolean = false,
        val currentTrack: RecentTrack? = null,
        val isNowPlaying: Boolean = false,
        val playcount: String = "—",
        val artistCount: String = "—",
        val error: String? = null,
    )

    var uiState by mutableStateOf(UiState())
        private set

    init { refresh() }

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
                    val recentJob = async { lastFmService.getRecentTracks(user = username, limit = 1) }
                    val infoJob   = async { lastFmService.getUserInfo(user = username) }
                    val recent = recentJob.await()
                    val info   = infoJob.await()
                    val track  = recent.recentTracks.tracks.firstOrNull()
                    uiState = UiState(
                        username     = username,
                        avatarUrl    = info.user.images?.getExtraLargeUrl(),
                        currentTrack = track,
                        isNowPlaying = track?.attr?.nowplaying == "true",
                        playcount    = info.user.playcount,
                        artistCount  = info.user.artistCount ?: "—",
                        isLoading    = false,
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "No se pudo cargar. Revisa tu conexión.")
            }
        }
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
