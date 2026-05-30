package com.jfcardenas.musicwall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.ui.navigation.MusicWallNav
import com.jfcardenas.musicwall.ui.theme.MusicWallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay cuenta conectada, ir directo al main app
        val prefs    = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
        val username = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (username.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MusicWallTheme {
                MusicWallNav(
                    onOnboardingComplete = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
