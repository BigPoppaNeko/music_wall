package com.jfcardenas.musicwall

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jfcardenas.musicwall.features.connect.ConnectActivity
import com.jfcardenas.musicwall.service.CollageWallpaper
import com.jfcardenas.musicwall.settings.WallpaperSettingsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs    = getSharedPreferences(CollageWallpaper.PREFS_NAME, MODE_PRIVATE)
        val username = prefs.getString(CollageWallpaper.PREF_USERNAME, "") ?: ""

        val next = if (username.isEmpty())
            Intent(this, ConnectActivity::class.java)
        else
            Intent(this, WallpaperSettingsActivity::class.java)

        startActivity(next)
        finish()
    }
}
