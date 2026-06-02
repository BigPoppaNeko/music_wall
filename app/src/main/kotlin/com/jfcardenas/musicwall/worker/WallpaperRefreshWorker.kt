package com.jfcardenas.musicwall.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jfcardenas.musicwall.service.CollageWallpaper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class WallpaperRefreshWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Scheduled refresh triggered")
        applicationContext.sendBroadcast(Intent(CollageWallpaper.ACTION_REFRESH))
        return Result.success()
    }

    companion object {
        private const val TAG = "WallpaperRefreshWorker"
        private const val WORK_NAME = "wallpaper_periodic_refresh"

        fun isEnabled(context: Context): Boolean =
            context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(CollageWallpaper.PREF_AUTO_WALLPAPER_REFRESH, true)

        fun scheduleIfEnabled(context: Context, intervalHours: Long = 24) {
            if (isEnabled(context)) schedule(context, intervalHours) else cancel(context)
        }

        fun setEnabled(context: Context, enabled: Boolean, intervalHours: Long = 24) {
            context.getSharedPreferences(CollageWallpaper.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(CollageWallpaper.PREF_AUTO_WALLPAPER_REFRESH, enabled)
                .apply()
            if (enabled) schedule(context, intervalHours) else cancel(context)
        }

        fun schedule(context: Context, intervalHours: Long = 24) {
            val request = PeriodicWorkRequestBuilder<WallpaperRefreshWorker>(
                repeatInterval = intervalHours,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            Log.d(TAG, "Scheduled periodic refresh every ${intervalHours}h")
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled periodic refresh")
        }
    }
}
