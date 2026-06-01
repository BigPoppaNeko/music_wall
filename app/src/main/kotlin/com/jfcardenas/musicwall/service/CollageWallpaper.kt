package com.jfcardenas.musicwall.service

import android.app.WallpaperManager
import android.content.*
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Size
import dagger.hilt.android.AndroidEntryPoint
import com.jfcardenas.musicwall.data.ErrorType
import com.jfcardenas.musicwall.data.NetworkResult
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase
import com.jfcardenas.musicwall.features.wallpaper.renderer.RenderItem
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRendererFactory
import com.jfcardenas.musicwall.worker.WallpaperRefreshWorker
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class CollageWallpaper : WallpaperService() {

    companion object {
        const val PREFS_NAME        = "music_wall_prefs"
        const val PREF_USERNAME     = "lastfm_username"
        const val PREF_IMAGE_KIND   = "image_kind"
        const val PREF_PERIOD       = "period"
        const val PREF_LIMIT        = "limit"
        const val PREF_COLLAGE_PATH  = "collage_path"
        const val PREF_REFRESH_COUNT = "refresh_count"
        const val ACTION_REFRESH     = "com.jfcardenas.musicwall.action.REFRESH"
        private const val TAG       = "MusicWall"
        private const val MIN_IMAGES = 4
    }

    @Inject lateinit var getMusicImages: GetMusicImagesUseCase
    @Inject lateinit var rendererFactory: WallpaperRendererFactory
    @Inject lateinit var imageLoader: ImageLoader

    private var engine: CollageEngine? = null

    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "━━━ [Receiver] ACTION_REFRESH recibido — engine=${if (engine != null) "OK" else "NULL"}")
            engine?.startRefresh()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "━━━ [Service] onCreate")
        ContextCompat.registerReceiver(
            this, refreshReceiver,
            IntentFilter(ACTION_REFRESH),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        WallpaperRefreshWorker.schedule(this)
    }

    override fun onDestroy() {
        Log.d(TAG, "━━━ [Service] onDestroy")
        unregisterReceiver(refreshReceiver)
        super.onDestroy()
    }

    override fun onCreateEngine(): Engine {
        Log.d(TAG, "━━━ [Service] onCreateEngine")
        return CollageEngine().also { engine = it }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            imageLoader.memoryCache?.clear()
            Log.d(TAG, "▸ Memory trimmed at level $level")
        }
    }

    private fun wallpaperSize(): Pair<Int, Int> {
        val dm = resources.displayMetrics
        return dm.widthPixels to dm.heightPixels
    }

    private suspend fun downloadBitmap(url: String): Bitmap? {
        repeat(3) { attempt ->
            try {
                val request = ImageRequest.Builder(applicationContext)
                    .data(url)
                    .size(Size.ORIGINAL)
                    .allowHardware(false)
                    .build()
                val bmp = (imageLoader.execute(request).drawable
                        as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bmp != null) return bmp
            } catch (e: Exception) {
                Log.w(TAG, "✗ Download attempt ${attempt + 1}/3: $url — ${e.message}")
            }
            if (attempt < 2) delay(300L * (attempt + 1))
        }
        return null
    }

    inner class CollageEngine : Engine() {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        private var bitmap: Bitmap? = null
        private var offsetX = 0f
        private var offsetY = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            // Wallpaper estático: no se desplaza entre pantallas del launcher
            setOffsetNotificationsEnabled(false)
            loadCachedCollage()
        }

        override fun onDestroy() {
            scope.cancel()
            if (engine === this) engine = null
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) drawFrame()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float,
            xStep: Float, yStep: Float,
            xPixels: Int, yPixels: Int
        ) {
            offsetX = xPixels.toFloat()
            offsetY = yPixels.toFloat()
            drawFrame()
        }

        fun startRefresh() {
            scope.launch { refresh() }
        }

        private fun loadCachedCollage() {
            val path = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(PREF_COLLAGE_PATH, "") ?: ""
            if (path.isEmpty()) return
            val file = File(path)
            if (!file.exists()) { Log.w(TAG, "✗ Caché no existe: $path"); return }
            bitmap = BitmapFactory.decodeFile(path)
            Log.d(TAG, "✓ Caché cargado: ${bitmap?.width}x${bitmap?.height}")
            drawFrame()
        }

        private suspend fun refresh() {
            Log.d(TAG, "━━━ refresh()")
            val prefs     = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val source    = prefs.getString(PREF_SOURCE, PREF_SOURCE_LASTFM) ?: PREF_SOURCE_LASTFM
            val username  = prefs.getString(PREF_USERNAME, "") ?: ""
            if (source != PREF_SOURCE_EXPLORE_ARTISTS && username.isEmpty()) {
                toast("Configura tu usuario de Last.fm en ajustes")
                return
            }

            val imageKind = prefs.getString(PREF_IMAGE_KIND, "ALBUMS") ?: "ALBUMS"
            val period    = prefs.getString(PREF_PERIOD, "7day") ?: "7day"
            val limit     = prefs.getString(PREF_LIMIT, "25")?.toIntOrNull() ?: 25
            val exploreArtists = prefs.getStringSet(PREF_EXPLORE_ARTISTS, emptySet())?.toList().orEmpty()
            Log.d(TAG, "▸ source=$source kind=$imageKind period=$period limit=$limit")
            toast(if (source == PREF_SOURCE_EXPLORE_ARTISTS) "Explorando álbumes en Last.fm…" else "Obteniendo tus tops de Last.fm…")

            val result = if (source == PREF_SOURCE_EXPLORE_ARTISTS) {
                getMusicImages.artistCatalogAlbums(exploreArtists, limit, forceRefresh = true)
            } else {
                getMusicImages(username, imageKind, period, limit, forceRefresh = period == "random")
            }

            when (result) {
                is NetworkResult.Success -> {
                    val musicImages = result.data
                    Log.d(TAG, "✓ ${musicImages.size} imágenes")
                    toast("Descargando ${musicImages.size} imágenes…")

                    val renderItems = withContext(Dispatchers.IO) {
                        musicImages.mapNotNull { img ->
                            downloadBitmap(img.url)?.let { bmp -> RenderItem(bmp, img) }
                        }
                    }

                    val loaded = renderItems.size
                    val total  = musicImages.size
                    Log.d(TAG, "✓ $loaded/$total imágenes descargadas")

                    if (loaded < MIN_IMAGES) {
                        toast("Solo $loaded/$total imágenes disponibles")
                        return
                    }

                    val refreshCount = prefs.getInt(PREF_REFRESH_COUNT, 0)
                    val rotatedItems = if (renderItems.size > 1) {
                        val offset = refreshCount % renderItems.size
                        renderItems.drop(offset) + renderItems.take(offset)
                    } else renderItems

                    val (w, h) = wallpaperSize()
                    val collageBitmap = withContext(Dispatchers.Default) {
                        rendererFactory.get().render(rotatedItems, w, h)
                    }
                    Log.d(TAG, "✓ Collage ${collageBitmap.width}x${collageBitmap.height}")

                    val path = withContext(Dispatchers.IO) { saveBitmap(collageBitmap) }
                    bitmap = collageBitmap
                    drawFrame()
                    prefs.edit()
                        .putString(PREF_COLLAGE_PATH, path)
                        .putInt(PREF_REFRESH_COUNT, refreshCount + 1)
                        .apply()
                    toast("¡Collage listo!")
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "✗ [${result.type}]: ${result.message}")
                    toast(result.toUserMessage())
                }
            }
        }

        private fun saveBitmap(bmp: Bitmap): String {
            val file = File(cacheDir, "collage.jpg")
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 92, it) }
            return file.absolutePath
        }

        fun drawFrame() {
            val bmp = bitmap ?: return
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                val frame = surfaceHolder.surfaceFrame
                val sw = frame.width().toFloat()
                val sh = frame.height().toFloat()
                val bw = bmp.width.toFloat()
                val bh = bmp.height.toFloat()

                // Escalar bitmap para cubrir la pantalla sin barras negras (center-crop)
                val scale = maxOf(sw / bw, sh / bh)
                val scaledW = bw * scale
                val scaledH = bh * scale

                val dx = (sw - scaledW) / 2f
                val dy = (sh - scaledH) / 2f

                val matrix = Matrix().apply {
                    setScale(scale, scale)
                    postTranslate(dx, dy)
                }
                canvas.drawBitmap(bmp, matrix, null)
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        private fun toast(msg: String) = Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun NetworkResult.Error.toUserMessage() = when (type) {
        ErrorType.NO_INTERNET     -> "Sin conexión a Internet"
        ErrorType.USER_NOT_FOUND  -> "Usuario de Last.fm no encontrado"
        ErrorType.RATE_LIMIT      -> "Demasiadas peticiones, espera un momento"
        ErrorType.API_KEY_INVALID -> "API key inválida — revisa local.properties"
        ErrorType.EMPTY_RESPONSE  -> "No hay historial para este período"
        ErrorType.SERVER_ERROR    -> "Error del servidor de Last.fm"
        ErrorType.UNKNOWN         -> "Error: $message"
    }
}
