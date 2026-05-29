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
        const val PREF_COLLAGE_PATH = "collage_path"
        const val ACTION_REFRESH    = "com.jfcardenas.musicwall.action.REFRESH"
        private const val TAG       = "MusicWall"
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
        val wm = getSystemService(WALLPAPER_SERVICE) as WallpaperManager
        val w = wm.desiredMinimumWidth.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
        val h = wm.desiredMinimumHeight.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels
        return w to h
    }

    private suspend fun downloadBitmap(url: String): Bitmap? {
        val request = ImageRequest.Builder(applicationContext)
            .data(url)
            .size(Size.ORIGINAL)
            .allowHardware(false)
            .build()
        return try {
            val bmp = (imageLoader.execute(request).drawable
                    as? android.graphics.drawable.BitmapDrawable)?.bitmap
            if (bmp == null) Log.w(TAG, "✗ Bitmap null: $url")
            bmp
        } catch (e: Exception) {
            Log.w(TAG, "✗ Download error: $url — ${e.message}")
            null
        }
    }

    inner class CollageEngine : Engine() {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        private var bitmap: Bitmap? = null
        private var offsetX = 0f
        private var offsetY = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
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
            val username  = prefs.getString(PREF_USERNAME, "") ?: ""
            if (username.isEmpty()) {
                toast("Configura tu usuario de Last.fm en ajustes")
                return
            }

            val imageKind = prefs.getString(PREF_IMAGE_KIND, "ALBUMS") ?: "ALBUMS"
            val period    = prefs.getString(PREF_PERIOD, "7day") ?: "7day"
            val limit     = prefs.getString(PREF_LIMIT, "25")?.toIntOrNull() ?: 25
            Log.d(TAG, "▸ kind=$imageKind period=$period limit=$limit")
            toast("Obteniendo tus tops de Last.fm…")

            when (val result = getMusicImages(username, imageKind, period, limit)) {
                is NetworkResult.Success -> {
                    val musicImages = result.data
                    Log.d(TAG, "✓ ${musicImages.size} imágenes")
                    toast("Descargando ${musicImages.size} imágenes…")

                    val renderItems = withContext(Dispatchers.IO) {
                        musicImages.mapNotNull { img ->
                            downloadBitmap(img.url)?.let { bmp -> RenderItem(bmp, img) }
                        }
                    }

                    if (renderItems.isEmpty()) {
                        toast("No se pudieron descargar las imágenes")
                        return
                    }

                    val (w, h) = wallpaperSize()
                    val collageBitmap = withContext(Dispatchers.Default) {
                        rendererFactory.get().render(renderItems, w, h)
                    }
                    Log.d(TAG, "✓ Collage ${collageBitmap.width}x${collageBitmap.height}")

                    val path = withContext(Dispatchers.IO) { saveBitmap(collageBitmap) }
                    bitmap = collageBitmap
                    drawFrame()
                    prefs.edit().putString(PREF_COLLAGE_PATH, path).apply()
                    toast("¡Collage listo!")
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "✗ [${result.type}]: ${result.message}")
                    toast(result.toUserMessage())
                }
            }
        }

        private fun saveBitmap(bmp: Bitmap): String {
            val file = File(cacheDir, "collage.png")
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 90, it) }
            return file.absolutePath
        }

        fun drawFrame() {
            val bmp = bitmap ?: return
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                canvas.save()
                canvas.translate(offsetX, offsetY)
                canvas.drawBitmap(bmp, 0f, 0f, null)
                canvas.restore()
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
