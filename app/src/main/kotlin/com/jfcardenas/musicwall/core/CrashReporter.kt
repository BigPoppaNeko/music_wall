package com.jfcardenas.musicwall.core

import android.app.Application
import android.util.Log
import com.jfcardenas.musicwall.BuildConfig

/**
 * Captura crashes no controlados. Play Vitals cubre producción tras subir a Play Console.
 * Para Firebase Crashlytics: añade google-services.json y el plugin (ver local.properties.example).
 */
object CrashReporter {

    private const val TAG = "MusicWallCrash"

    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught on ${thread.name}", throwable)
            previous?.uncaughtException(thread, throwable)
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "CrashReporter installed")
        }
    }
}
