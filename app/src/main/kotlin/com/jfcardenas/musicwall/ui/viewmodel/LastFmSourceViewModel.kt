package com.jfcardenas.musicwall.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfcardenas.musicwall.api.LastFmApiException
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.data.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LastFmSourceViewModel @Inject constructor(
    private val lastFmService: LastFmService,
    private val settings: UserSettingsRepository,
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
                settings.saveLastFmConnection(user.name)
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
