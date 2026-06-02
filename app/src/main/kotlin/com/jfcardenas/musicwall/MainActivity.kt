package com.jfcardenas.musicwall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.service.ScrobbleForegroundService
import com.jfcardenas.musicwall.ui.navigation.MainNav
import com.jfcardenas.musicwall.ui.theme.MusicWallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_ROUTE = "open_route"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
        val legacyUsername = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""
        if (!prefs.getBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, false) &&
            legacyUsername.isNotEmpty() &&
            prefs.getString(CollageWallpaper.PREF_USER_ID, "").isNullOrBlank()
        ) {
            prefs.edit()
                .putBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, true)
                .putString(CollageWallpaper.PREF_USER_ID, "lastfm_$legacyUsername")
                .putString(CollageWallpaper.PREF_USER_NAME, legacyUsername)
                .apply()
        }
        val onboardingComplete = prefs.getBoolean(CollageWallpaper.PREF_ONBOARDING_COMPLETE, false)
        val userId = prefs.getString(CollageWallpaper.PREF_USER_ID, "") ?: ""
        if (!onboardingComplete || (userId.isEmpty() && legacyUsername.isEmpty())) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (ScrobbleForegroundService.isListenerEnabled(this)) {
            ScrobbleForegroundService.start(this)
        }

        setContent {
            MusicWallTheme {
                MainNav(
                    initialRoute = intent?.getStringExtra(EXTRA_OPEN_ROUTE)?.also {
                        intent?.removeExtra(EXTRA_OPEN_ROUTE)
                    },
                )
            }
        }
    }
}
