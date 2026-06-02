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

        // Si ya hay sesión activa, ir directo al main app
        val prefs    = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
        val onboardingComplete = prefs.getBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, false)
        val userId = prefs.getString(CollageWallpaper.PREF_USER_ID, "") ?: ""
        val legacyUsername = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (!onboardingComplete && legacyUsername.isNotEmpty() && userId.isEmpty()) {
            prefs.edit()
                .putBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, true)
                .putString(CollageWallpaper.PREF_USER_ID, "lastfm_$legacyUsername")
                .putString(CollageWallpaper.PREF_USER_NAME, legacyUsername)
                .putString(CollageWallpaper.PREF_AUTH_PROVIDER, CollageWallpaper.PREF_SOURCE_LASTFM)
                .apply()
        }
        if (prefs.getBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, false) &&
            (prefs.getString(CollageWallpaper.PREF_USER_ID, "")?.isNotEmpty() == true ||
                prefs.getString(CollageWallpaper.PREF_USERNAME, "")?.isNotEmpty() == true)
        ) {
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
