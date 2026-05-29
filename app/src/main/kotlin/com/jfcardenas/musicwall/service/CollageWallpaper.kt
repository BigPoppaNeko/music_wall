package com.jfcardenas.musicwall.service

import android.app.WallpaperManager
import android.content.*
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import com.jfcardenas.musicwall.BuildConfig
import com.jfcardenas.musicwall.api.LastFmApi
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.collage.CollageMaker
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class CollageWallpaper : WallpaperService() {

    companion object {
        const val PREFS_NAME = "music_wall_prefs"
        const val PREF_USERNAME = "lastfm_username"
        const val PREF_IMAGE_KIND = "image_kind"
        const val PREF_PERIOD = "period"
        const val PREF_LIMIT = "limit"
        const val PREF_COLLAGE_PATH = "collage_path"
        private const val TAG = "MusicWall"
    }

    private val httpClient = OkHttpClient()
    private var engine: CollageEngine? = null

    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            engine?.startRefresh()
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(refreshReceiver, IntentFilter(Intent.ACTION_RUN))
    }

    override fun onDestroy() {
        unregisterReceiver(refreshReceiver)
        super.onDestroy()
    }

    override fun onCreateEngine(): Engine = CollageEngine().also { engine = it }

    private fun wallpaperSize(): Pair<Int, Int> {
        val wm = getSystemService(WALLPAPER_SERVICE) as WallpaperManager
        val w = wm.desiredMinimumWidth.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels
        val h = wm.desiredMinimumHeight.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels
        return w to h
    }

    private suspend fun downloadBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(Request.Builder().url(url).build()).execute()
            response.body?.byteStream()?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Download failed: $url — ${e.message}")
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
            if (path.isNotEmpty() && File(path).exists()) {
                bitmap = BitmapFactory.decodeFile(path)
                drawFrame()
            }
        }

        private suspend fun refresh() {
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val username = prefs.getString(PREF_USERNAME, "") ?: ""
            val apiKey = BuildConfig.LASTFM_API_KEY

            if (username.isEmpty()) {
                toast("Configura tu usuario de Last.fm en ajustes")
                return
            }
            if (apiKey.isEmpty()) {
                toast("Falta la API key de Last.fm en local.properties")
                return
            }

            val imageKind = prefs.getString(PREF_IMAGE_KIND, "ALBUMS") ?: "ALBUMS"
            val period = prefs.getString(PREF_PERIOD, "7day") ?: "7day"
            val limit = prefs.getString(PREF_LIMIT, "25")?.toIntOrNull() ?: 25

            toast("Obteniendo tus tops de Last.fm…")

            try {
                val urls = withContext(Dispatchers.IO) {
                    if (imageKind == "ALBUMS") {
                        LastFmApi.service
                            .getTopAlbums(user = username, period = period, limit = limit, apiKey = apiKey)
                            .topAlbums.albums
                            .mapNotNull { it.images.getExtraLargeUrl() }
                    } else {
                        LastFmApi.service
                            .getTopArtists(user = username, period = period, limit = limit, apiKey = apiKey)
                            .topArtists.artists
                            .mapNotNull { it.images?.getExtraLargeUrl() }
                    }
                }

                if (urls.isEmpty()) {
                    toast("No se encontraron imágenes para este usuario y período")
                    return
                }

                toast("Descargando ${urls.size} imágenes…")

                val bitmaps = withContext(Dispatchers.IO) {
                    urls.mapNotNull { downloadBitmap(it) }
                }

                if (bitmaps.isEmpty()) {
                    toast("Error al descargar las imágenes")
                    return
                }

                val (w, h) = wallpaperSize()
                val collageBitmap = withContext(Dispatchers.Default) {
                    CollageMaker(w, h).createCollage(bitmaps)
                }

                val path = withContext(Dispatchers.IO) { saveBitmap(collageBitmap) }

                bitmap = collageBitmap
                drawFrame()
                drawFrame() // segundo draw para asegurar que se vea en el primer arranque

                prefs.edit().putString(PREF_COLLAGE_PATH, path).apply()
                toast("¡Collage listo!")

            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing collage", e)
                toast("Error: ${e.message}")
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

        private fun toast(msg: String) {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
