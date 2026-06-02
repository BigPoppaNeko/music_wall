package com.jfcardenas.musicwall.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jfcardenas.musicwall.MainActivity
import com.jfcardenas.musicwall.R
import com.jfcardenas.musicwall.auth.UserSessionRepository
import com.jfcardenas.musicwall.scrobble.NowPlayingBus
import com.jfcardenas.musicwall.scrobble.NowPlayingReader
import com.jfcardenas.musicwall.scrobble.ScrobbleEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScrobbleForegroundService : Service() {

    @Inject lateinit var nowPlayingReader: NowPlayingReader
    @Inject lateinit var scrobbleEngine: ScrobbleEngine
    @Inject lateinit var userSession: UserSessionRepository
    @Inject lateinit var nowPlayingBus: NowPlayingBus

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var busJob: Job? = null
    private var tickJob: Job? = null
    private var hasActiveTrack = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        busJob = scope.launch {
            nowPlayingBus.updates.collect { pollNowPlaying() }
        }
        tickJob = scope.launch {
            while (true) {
                delay(TICK_MS)
                if (hasActiveTrack) pollNowPlaying()
            }
        }
        scope.launch { pollNowPlaying() }
    }

    override fun onDestroy() {
        busJob?.cancel()
        tickJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun pollNowPlaying() {
        val userId = userSession.getUserId()
        if (userId.isBlank()) return
        val raw = nowPlayingReader.readActive()
        hasActiveTrack = raw != null
        if (raw != null) {
            scrobbleEngine.onNowPlaying(userId, raw)
            scrobbleEngine.onTick(userId, raw.positionMs)
        } else {
            scrobbleEngine.onPlaybackStopped(userId)
        }
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.scrobble_notification_title))
            .setContentText(getString(R.string.scrobble_notification_body))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.scrobble_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "music_wall_scrobbler"
        private const val NOTIFICATION_ID = 42
        /** Intervalo para reglas de scrobble (30s / 50%); solo corre si hay pista activa. */
        private const val TICK_MS = 15_000L

        fun start(context: Context) {
            val intent = Intent(context, ScrobbleForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun isListenerEnabled(context: Context): Boolean {
            val enabled = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners",
            ) ?: return false
            return enabled.contains(context.packageName)
        }

        fun openListenerSettings(context: Context) {
            context.startActivity(
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
