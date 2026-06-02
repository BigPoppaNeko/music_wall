package com.jfcardenas.musicwall.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jfcardenas.musicwall.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/** Punto de entrada del selector de wallpaper del sistema → flujo Compose de Murales. */
@AndroidEntryPoint
class WallpaperSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_ROUTE, "murales")
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        finish()
    }
}
