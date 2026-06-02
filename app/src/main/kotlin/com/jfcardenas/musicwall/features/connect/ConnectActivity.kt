package com.jfcardenas.musicwall.features.connect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jfcardenas.musicwall.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/** Legacy entry — redirige al flujo Compose de la app principal. */
@AndroidEntryPoint
class ConnectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_OPEN_ROUTE, "source/lastfm")
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        finish()
    }
}
