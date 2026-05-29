package com.jfcardenas.musicwall.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jfcardenas.musicwall.R

class WallpaperSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
}
