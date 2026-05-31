package com.jfcardenas.musicwall.ui.viewmodel

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.jfcardenas.musicwall.data.ErrorType
import com.jfcardenas.musicwall.data.NetworkResult
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase
import com.jfcardenas.musicwall.features.wallpaper.renderer.RenderItem
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRendererFactory
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class GeneratingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getMusicImages: GetMusicImagesUseCase,
    private val rendererFactory: WallpaperRendererFactory,
    private val imageLoader: ImageLoader,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    sealed interface UiState {
        data object Idle : UiState
        data class Loading(val progress: Float, val stage: Int) : UiState
        data object Done : UiState
        data class Error(val message: String) : UiState
    }

    var state by mutableStateOf<UiState>(UiState.Idle)
        private set

    private val styleId: String = savedStateHandle.get<String>("styleId") ?: "street"

    init {
        rendererFactory.select(styleId)
        start()
    }

    fun retry() = start()

    private fun start() {
        state = UiState.Loading(0f, 0)
        viewModelScope.launch {
            try {
                generate()
            } catch (e: Exception) {
                state = UiState.Error("Error inesperado: ${e.javaClass.simpleName} — ${e.message}")
            }
        }
    }

    private suspend fun generate() {
        val prefs    = context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""

        if (username.isEmpty()) {
            state = UiState.Error("Configura tu usuario de Last.fm primero")
            return
        }

        val imageKind = prefs.getString(CollageWallpaper.PREF_IMAGE_KIND, "ALBUMS") ?: "ALBUMS"
        val rawPeriod = prefs.getString(CollageWallpaper.PREF_PERIOD, "7day") ?: "7day"
        val limit     = prefs.getString(CollageWallpaper.PREF_LIMIT, "25")?.toIntOrNull() ?: 25
        val isRandom  = rawPeriod == "random"
        val apiPeriod = if (isRandom) "overall" else rawPeriod
        val fetchLimit = if (isRandom) 50 else limit

        // Stage 0 — obtener datos de Last.fm
        state = UiState.Loading(0.05f, 0)
        val musicImages = when (val result = getMusicImages(username, imageKind, apiPeriod, fetchLimit)) {
            is NetworkResult.Success -> if (isRandom) result.data.shuffled() else result.data
            is NetworkResult.Error   -> { state = UiState.Error(result.toUserMessage()); return }
        }

        // Stage 1 — descargar bitmaps con progreso real
        state = UiState.Loading(0.25f, 1)
        val renderItems = mutableListOf<RenderItem>()
        musicImages.forEachIndexed { index, img ->
            downloadBitmap(img.url)?.let { bmp -> renderItems.add(RenderItem(bmp, img)) }
            state = UiState.Loading(
                progress = 0.25f + (index + 1).toFloat() / musicImages.size * 0.50f,
                stage    = 1,
            )
        }

        if (renderItems.size < 4) {
            state = UiState.Error("Solo ${renderItems.size} imágenes disponibles. Necesitas al menos 4.")
            return
        }

        // Stage 2 — renderizar
        state = UiState.Loading(0.78f, 2)
        val w = context.resources.displayMetrics.widthPixels
        val h = context.resources.displayMetrics.heightPixels
        val collageBitmap = withContext(Dispatchers.Default) {
            rendererFactory.get().render(renderItems, w, h)
        }

        // Stage 3 — guardar
        state = UiState.Loading(0.93f, 3)
        val path = withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "collage.jpg")
            FileOutputStream(file).use { collageBitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            file.absolutePath
        }
        prefs.edit().putString(CollageWallpaper.PREF_COLLAGE_PATH, path).apply()

        state = UiState.Done
    }

    private suspend fun downloadBitmap(url: String): Bitmap? {
        repeat(3) { attempt ->
            try {
                val req = ImageRequest.Builder(context)
                    .data(url)
                    .size(Size.ORIGINAL)
                    .allowHardware(false)
                    .build()
                val bmp = (imageLoader.execute(req).drawable as? BitmapDrawable)?.bitmap
                if (bmp != null) return bmp
            } catch (_: Exception) { }
            if (attempt < 2) delay(300L * (attempt + 1))
        }
        return null
    }

    private fun NetworkResult.Error.toUserMessage() = when (type) {
        ErrorType.NO_INTERNET     -> "Sin conexión a Internet"
        ErrorType.USER_NOT_FOUND  -> "Usuario de Last.fm no encontrado"
        ErrorType.RATE_LIMIT      -> "Demasiadas peticiones, espera un momento"
        ErrorType.API_KEY_INVALID -> "API key inválida"
        ErrorType.EMPTY_RESPONSE  -> "Sin historial para este período"
        ErrorType.SERVER_ERROR    -> "Error del servidor de Last.fm"
        ErrorType.UNKNOWN         -> "Error: $message"
    }
}
