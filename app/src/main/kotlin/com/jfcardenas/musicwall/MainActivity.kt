package com.jfcardenas.musicwall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.ui.navigation.MainNav
import com.jfcardenas.musicwall.ui.theme.MusicWallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Guard: si no hay cuenta conectada, ir al onboarding
        val username = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
            .getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (username.isEmpty()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MusicWallTheme {
                MainNav()
            }
        }
    }
}
