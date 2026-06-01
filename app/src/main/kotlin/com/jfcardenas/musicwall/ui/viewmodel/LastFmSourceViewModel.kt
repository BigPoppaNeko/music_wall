package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.LastFmApiException
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LastFmSourceViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Error(val message: String) : State()
        data class Success(val username: String) : State()
    }

    var state by mutableStateOf<State>(State.Idle)
        private set

    fun connect(username: String) {
        if (username.isBlank()) {
            state = State.Error("Ingresa tu usuario de Last.fm")
            return
        }
        state = State.Loading
        viewModelScope.launch {
            try {
                val user = lastFmService.getUserInfo(user = username.trim()).user
                context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(CollageWallpaper.PREF_USERNAME, user.name)
                    .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_LASTFM)
                    .apply()
                state = State.Success(user.name)
            } catch (e: LastFmApiException) {
                state = State.Error(
                    if (e.code == 6) "Usuario no encontrado en Last.fm"
                    else "Error al conectar. Intenta de nuevo."
                )
            } catch (e: Exception) {
                state = State.Error("Sin conexión. Verifica tu internet.")
            }
        }
    }

    fun clearError() {
        if (state is State.Error) state = State.Idle
    }
}
