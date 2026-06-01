package com.jfcardenas.musicwall.features.connect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.jfcardenas.musicwall.R
import com.jfcardenas.musicwall.api.LastFmApiException
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.settings.WallpaperSettingsActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ConnectActivity : AppCompatActivity() {

    @Inject lateinit var lastFmService: LastFmService

    private lateinit var tilUsername: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var btnConnect: MaterialButton
    private lateinit var progress: LinearProgressIndicator
    private lateinit var tvCreateAccount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_connect)

        tilUsername    = findViewById(R.id.til_username)
        etUsername     = findViewById(R.id.et_username)
        btnConnect     = findViewById(R.id.btn_connect)
        progress       = findViewById(R.id.progress)
        tvCreateAccount = findViewById(R.id.tv_create_account)

        // Si ya hay un usuario guardado, precargarlo
        val prefs = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
        val saved = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (saved.isNotEmpty()) etUsername.setText(saved)

        btnConnect.setOnClickListener { verify() }

        etUsername.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { verify(); true } else false
        }

        tvCreateAccount.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.last.fm/join")))
        }
    }

    private fun verify() {
        val username = etUsername.text?.toString()?.trim() ?: ""

        tilUsername.error = null

        if (username.isEmpty()) {
            tilUsername.error = getString(R.string.error_empty_username)
            return
        }

        hideKeyboard()
        setLoading(true)

        lifecycleScope.launch {
            try {
                val user = lastFmService.getUserInfo(user = username).user

                // Guardar username y nombre real
                getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(CollageWallpaper.PREF_USERNAME, user.name)
                    .putString(CollageWallpaper.PREF_SOURCE, CollageWallpaper.PREF_SOURCE_LASTFM)
                    .apply()

                // Ir a ajustes
                startActivity(
                    Intent(this@ConnectActivity, WallpaperSettingsActivity::class.java)
                        .putExtra(WallpaperSettingsActivity.EXTRA_SHOW_CONNECTED_TOAST, user.name)
                )
                finish()

            } catch (e: LastFmApiException) {
                setLoading(false)
                tilUsername.error = when (e.code) {
                    6 -> getString(R.string.error_user_not_found)
                    else -> getString(R.string.error_generic)
                }
            } catch (e: IOException) {
                setLoading(false)
                tilUsername.error = getString(R.string.error_network)
            } catch (e: Exception) {
                setLoading(false)
                tilUsername.error = getString(R.string.error_generic)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnConnect.isEnabled = !loading
        etUsername.isEnabled = !loading
        progress.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        if (loading) btnConnect.text = getString(R.string.verifying)
        else btnConnect.text = getString(R.string.btn_connect)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etUsername.windowToken, 0)
    }
}
