package com.jfcardenas.musicwall.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jfcardenas.musicwall.data.UserSettingsRepository
import com.jfcardenas.musicwall.worker.WallpaperRefreshWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: UserSettingsRepository,
) : ViewModel() {

    var autoChange by mutableStateOf(settings.isAutoWallpaperRefreshEnabled())
        private set

    var lastFmUsername by mutableStateOf(settings.getLastFmUsername())
        private set

    fun disconnectLastFm() {
        settings.clearLastFmConnection()
        lastFmUsername = ""
    }

    fun reload() {
        lastFmUsername = settings.getLastFmUsername()
        autoChange = settings.isAutoWallpaperRefreshEnabled()
    }

    fun setAutoChange(context: Context, enabled: Boolean) {
        autoChange = enabled
        settings.setAutoWallpaperRefreshEnabled(enabled)
        if (enabled) {
            WallpaperRefreshWorker.schedule(context)
        } else {
            WallpaperRefreshWorker.cancel(context)
        }
    }
}
