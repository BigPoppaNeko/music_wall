package com.jfcardenas.musicwall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.SpotifyOEmbedService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpotifySourceViewModel @Inject constructor(
    private val spotifyService: SpotifyOEmbedService,
) : ViewModel() {

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Error(val message: String) : State()
        data class Preview(val title: String, val author: String) : State()
    }

    var state by mutableStateOf<State>(State.Idle)
        private set

    fun fetchPreview(url: String) {
        val clean = url.trim()
        if (!clean.contains("spotify.com/playlist/")) {
            state = State.Error("Pega un enlace de playlist de Spotify")
            return
        }
        state = State.Loading
        viewModelScope.launch {
            try {
                val data = spotifyService.getPlaylistInfo(clean)
                state = State.Preview(data.title, data.authorName)
            } catch (e: Exception) {
                state = State.Error("No se pudo obtener la playlist. Verifica el enlace.")
            }
        }
    }

    fun clearError() {
        if (state is State.Error) state = State.Idle
    }

    fun reset() {
        state = State.Idle
    }
}
