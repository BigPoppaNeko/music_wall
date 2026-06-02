package com.jfcardenas.musicwall.scrobble

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import com.jfcardenas.musicwall.service.NowPlayingListenerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NowPlayingReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun readActive(): RawNowPlaying? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null
        val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            ?: return null
        val component = ComponentName(context, NowPlayingListenerService::class.java)
        val sessions = try {
            manager.getActiveSessions(component)
        } catch (_: SecurityException) {
            return null
        }
        for (controller in sessions) {
            readController(controller)?.let { return it }
        }
        return null
    }

    private fun readController(controller: MediaController): RawNowPlaying? {
        val metadata = controller.metadata ?: return null
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: return null
        val track = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: return null
        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty()
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(0L)
        val artwork = metadata.getString(MediaMetadata.METADATA_KEY_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
        val position = controller.playbackState?.position?.coerceAtLeast(0L) ?: 0L
        val playing = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        if (!playing && position <= 0L) return null
        return RawNowPlaying(
            artist = artist,
            track = track,
            album = album,
            durationMs = duration,
            positionMs = position,
            sourceApp = controller.packageName,
            artworkUrl = artwork,
        )
    }
}
