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
        val normalized = normalizeSpotifyUrl(url)
        if (!normalized.contains("spotify.com/playlist/")) {
            state = State.Error("Pega un enlace de playlist de Spotify")
            return
        }
        state = State.Loading
        viewModelScope.launch {
            try {
                val data = spotifyService.getPlaylistInfo(normalized)
                state = State.Preview(data.title, data.authorName)
            } catch (e: Exception) {
                state = State.Error("No se pudo obtener la playlist. Verifica el enlace.")
            }
        }
    }

    private fun normalizeSpotifyUrl(input: String): String {
        val s = input.trim()
        // spotify:playlist:ID  →  https://open.spotify.com/playlist/ID
        if (s.startsWith("spotify:playlist:")) {
            val id = s.removePrefix("spotify:playlist:").substringBefore("?").substringBefore(":")
            return "https://open.spotify.com/playlist/$id"
        }
        // Eliminar parámetros de rastreo innecesarios (mantiene el path limpio)
        return s.substringBefore("?").ifEmpty { s }
    }

    fun clearError() {
        if (state is State.Error) state = State.Idle
    }

    fun reset() {
        state = State.Idle
    }
}
