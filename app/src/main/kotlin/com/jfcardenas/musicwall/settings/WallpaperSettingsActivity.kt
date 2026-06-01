package com.jfcardenas.musicwall.settings

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jfcardenas.musicwall.R
import com.jfcardenas.musicwall.data.NetworkResult
import com.jfcardenas.musicwall.domain.model.MusicImage
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase
import com.jfcardenas.musicwall.features.connect.ConnectActivity
import com.jfcardenas.musicwall.features.wallpaper.renderer.RenderItem
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRendererFactory
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class WallpaperSettingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SHOW_CONNECTED_TOAST = "extra_connected_username"
        private const val TAG = "MusicWall"
        private const val MIN_IMAGES = 4
    }

    @Inject lateinit var getMusicImages: GetMusicImagesUseCase
    @Inject lateinit var imageLoader: ImageLoader
    @Inject lateinit var rendererFactory: WallpaperRendererFactory

    private lateinit var tilUsername: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var btnGroupImageKind: MaterialButtonToggleGroup
    private lateinit var tilPeriod: TextInputLayout
    private lateinit var acvPeriod: AutoCompleteTextView
    private lateinit var acvLimit: AutoCompleteTextView
    private lateinit var btnGenerate: MaterialButton
    private lateinit var btnChangeAccount: MaterialButton
    private lateinit var btnApplyWallpaper: MaterialButton
    private lateinit var tvStatus: TextView
    private lateinit var ivPreview: ImageView
    private lateinit var tvUsedLabel: TextView
    private lateinit var chipGroupUsedItems: ChipGroup

    private val periodEntries = arrayOf("Última semana", "Último mes", "3 meses", "6 meses", "12 meses", "General", "Aleatorio histórico")
    private val periodValues  = arrayOf("7day", "1month", "3month", "6month", "12month", "overall", "random")

    private val limitEntries = arrayOf("10 imágenes", "25 imágenes", "50 imágenes")
    private val limitValues  = arrayOf("10", "25", "50")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))

        tilUsername       = findViewById(R.id.til_username)
        etUsername        = findViewById(R.id.et_username)
        btnGroupImageKind = findViewById(R.id.btn_group_image_kind)
        tilPeriod         = findViewById(R.id.til_period)
        acvPeriod         = findViewById(R.id.acv_period)
        acvLimit          = findViewById(R.id.acv_limit)
        btnGenerate       = findViewById(R.id.btn_generate)
        btnChangeAccount  = findViewById(R.id.btn_change_account)
        btnApplyWallpaper = findViewById(R.id.btn_apply_wallpaper)
        tvStatus          = findViewById(R.id.tv_status)
        ivPreview         = findViewById(R.id.iv_preview)
        tvUsedLabel       = findViewById(R.id.tv_used_label)
        chipGroupUsedItems = findViewById(R.id.chip_group_used_items)

        setupDropdowns()
        loadPrefs()

        btnGenerate.setOnClickListener { generate() }
        btnChangeAccount.setOnClickListener { startActivity(Intent(this, ConnectActivity::class.java)) }
        btnApplyWallpaper.setOnClickListener { openWallpaperPicker() }

        etUsername.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { hideKeyboard(); true } else false
        }

        intent.getStringExtra(EXTRA_SHOW_CONNECTED_TOAST)?.let { username ->
            Toast.makeText(this, "¡Conectado como $username!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDropdowns() {
        val bg = ColorDrawable(Color.parseColor("#242020"))
        fun adapter(entries: Array<String>) = ArrayAdapter(this, R.layout.dropdown_item, entries)

        acvPeriod.setAdapter(adapter(periodEntries))
        acvPeriod.setDropDownBackgroundDrawable(bg)
        acvLimit.setAdapter(adapter(limitEntries))
        acvLimit.setDropDownBackgroundDrawable(bg)
    }

    private fun loadPrefs() {
        val p = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)

        etUsername.setText(p.getString(CollageWallpaper.PREF_USERNAME, "") ?: "")

        val kindValue = p.getString(CollageWallpaper.PREF_IMAGE_KIND, "ALBUMS") ?: "ALBUMS"
        btnGroupImageKind.check(
            if (kindValue == "ARTISTS") R.id.btn_kind_artists else R.id.btn_kind_albums
        )

        acvPeriod.setText(
            periodEntries[periodValues.indexOf(p.getString(CollageWallpaper.PREF_PERIOD, "7day")).coerceAtLeast(0)], false)

        acvLimit.setText(
            limitEntries[limitValues.indexOf(p.getString(CollageWallpaper.PREF_LIMIT, "25")).coerceAtLeast(0)], false)
    }

    private fun generate() {
        val username = etUsername.text?.toString()?.trim() ?: ""
        tilUsername.error = null
        tvStatus.visibility = View.GONE
        btnApplyWallpaper.visibility = View.GONE
        ivPreview.visibility = View.GONE
        tvUsedLabel.visibility = View.GONE
        chipGroupUsedItems.visibility = View.GONE

        if (username.isEmpty()) {
            tilUsername.error = "Introduce tu usuario de Last.fm"
            return
        }

        savePrefs(username)
        hideKeyboard()

        if (isWallpaperActive()) {
            sendBroadcast(Intent(CollageWallpaper.ACTION_REFRESH))
            showStatus("Generando wallpaper en segundo plano…", success = true)
        } else {
            generateDirectly(username)
        }

        btnGenerate.isEnabled = false
        btnGenerate.postDelayed({ btnGenerate.isEnabled = true }, 8_000)
    }

    private fun generateDirectly(username: String) {
        val prefs     = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
        val imageKind = prefs.getString(CollageWallpaper.PREF_IMAGE_KIND, "ALBUMS") ?: "ALBUMS"
        val period    = prefs.getString(CollageWallpaper.PREF_PERIOD, "7day") ?: "7day"
        val limit     = prefs.getString(CollageWallpaper.PREF_LIMIT, "25")?.toIntOrNull() ?: 25

        showStatus("Conectando con Last.fm…", success = true)

        lifecycleScope.launch {
            try {
                when (val result = getMusicImages(username, imageKind, period, limit, forceRefresh = period == "random")) {
                    is NetworkResult.Success -> {
                        val musicImages = result.data
                        if (musicImages.isEmpty()) {
                            showStatus("Sin resultados para este período", false)
                            return@launch
                        }

                        showStatus("Descargando ${musicImages.size} imágenes…", true)

                        val renderItems = withContext(Dispatchers.IO) {
                            musicImages.mapNotNull { img ->
                                downloadBitmapWithRetry(img.url)?.let { bmp -> RenderItem(bmp, img) }
                            }
                        }

                        val loaded = renderItems.size
                        val total  = musicImages.size
                        Log.d(TAG, "✓ $loaded/$total imágenes descargadas")

                        if (loaded < MIN_IMAGES) {
                            showStatus(
                                "Solo $loaded/$total imágenes disponibles. " +
                                "Prueba con otro período o más imágenes.",
                                false
                            )
                            return@launch
                        }

                        showStatus("Creando wallpaper…", true)
                        val w = resources.displayMetrics.widthPixels
                        val h = resources.displayMetrics.heightPixels

                        val collage = withContext(Dispatchers.Default) {
                            rendererFactory.get().render(renderItems, w, h)
                        }
                        Log.d(TAG, "✓ Collage ${collage.width}x${collage.height}")

                        val path = withContext(Dispatchers.IO) {
                            val file = File(cacheDir, "collage.png")
                            FileOutputStream(file).use { collage.compress(Bitmap.CompressFormat.PNG, 90, it) }
                            file.absolutePath
                        }
                        prefs.edit().putString(CollageWallpaper.PREF_COLLAGE_PATH, path).apply()

                        showStatus("¡Wallpaper listo! ($loaded imágenes usadas)", true)
                        btnApplyWallpaper.visibility = View.VISIBLE
                        ivPreview.setImageBitmap(collage)
                        ivPreview.visibility = View.VISIBLE

                        showUsedItems(imageKind, renderItems.map { it.image })
                    }
                    is NetworkResult.Error -> showStatus("Error: ${result.message}", false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ generateDirectly", e)
                showStatus("Error inesperado: ${e.message}", false)
            }
        }
    }

    private suspend fun downloadBitmapWithRetry(url: String): Bitmap? {
        repeat(3) { attempt ->
            try {
                val bmp = (imageLoader.execute(
                    ImageRequest.Builder(this).data(url).allowHardware(false).build()
                ).drawable as? BitmapDrawable)?.bitmap
                if (bmp != null) return bmp
            } catch (e: Exception) {
                Log.w(TAG, "Download attempt ${attempt + 1}/3 failed: $url — ${e.message}")
            }
            if (attempt < 2) delay(300L * (attempt + 1))
        }
        return null
    }

    private fun showUsedItems(imageKind: String, images: List<MusicImage>) {
        tvUsedLabel.text = if (imageKind == "ARTISTS") "ARTISTAS UTILIZADOS" else "ÁLBUMES UTILIZADOS"
        tvUsedLabel.visibility = View.VISIBLE

        chipGroupUsedItems.removeAllViews()
        val chipBg = ColorStateList.valueOf(Color.parseColor("#2A2A2A"))
        images.forEach { img ->
            val chip = Chip(this).apply {
                text = if (img.kind == MusicImage.Kind.ALBUM) "${img.name} · ${img.artistName}" else img.name
                isClickable = false
                isCheckable = false
                chipBackgroundColor = chipBg
                setTextColor(Color.WHITE)
                chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#444444"))
                chipStrokeWidth = 1f
            }
            chipGroupUsedItems.addView(chip)
        }
        chipGroupUsedItems.visibility = View.VISIBLE
    }

    private fun savePrefs(username: String) {
        val imageKind = if (btnGroupImageKind.checkedButtonId == R.id.btn_kind_artists) "ARTISTS" else "ALBUMS"
        val periodIdx = periodEntries.indexOf(acvPeriod.text.toString()).coerceAtLeast(0)
        val limitIdx  = limitEntries.indexOf(acvLimit.text.toString()).coerceAtLeast(0)

        getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE).edit()
            .putString(CollageWallpaper.PREF_USERNAME,   username)
            .putString(CollageWallpaper.PREF_SOURCE,     CollageWallpaper.PREF_SOURCE_LASTFM)
            .putString(CollageWallpaper.PREF_IMAGE_KIND, imageKind)
            .putString(CollageWallpaper.PREF_PERIOD,     periodValues[periodIdx])
            .putString(CollageWallpaper.PREF_LIMIT,      limitValues[limitIdx])
            .apply()
    }

    private fun isWallpaperActive(): Boolean = try {
        WallpaperManager.getInstance(this).wallpaperInfo
            ?.component?.className == CollageWallpaper::class.java.name
    } catch (e: Exception) { false }

    private fun openWallpaperPicker() {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@WallpaperSettingsActivity, CollageWallpaper::class.java))
        }
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        else startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
    }

    private fun showStatus(msg: String, success: Boolean) {
        tvStatus.text = msg
        tvStatus.setTextColor(if (success) Color.parseColor("#4CAF50") else Color.parseColor("#FF5252"))
        tvStatus.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(etUsername.windowToken, 0)
    }
}
