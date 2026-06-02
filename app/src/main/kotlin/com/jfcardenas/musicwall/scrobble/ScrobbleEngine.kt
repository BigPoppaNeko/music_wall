package com.jfcardenas.musicwall.scrobble

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class ScrobbleEngine @Inject constructor(
    private val repository: ScrobbleRepository,
) {
    companion object {
        private const val MIN_LISTEN_MS = 30_000L
        private const val SCROBBLE_MIN_MS = 240_000L
        private const val SCROBBLE_RATIO = 0.5f
    }

    private var currentKey: String? = null
    private var currentRaw: RawNowPlaying? = null
    private var startedAtMs: Long = 0L
    private var lastPositionMs: Long = 0L

    suspend fun onNowPlaying(userId: String, raw: RawNowPlaying) {
        val normalized = MetadataNormalizer.normalize(raw) ?: return
        val key = trackKey(normalized)

        if (key == currentKey) {
            lastPositionMs = raw.positionMs.coerceAtLeast(lastPositionMs)
            return
        }

        scrobbleCurrentIfEligible(userId)
        currentKey = key
        currentRaw = normalized
        startedAtMs = System.currentTimeMillis()
        lastPositionMs = normalized.positionMs
        repository.updateNowPlaying(userId, normalized)
    }

    suspend fun onTick(userId: String, positionMs: Long) {
        if (currentRaw == null) return
        lastPositionMs = positionMs.coerceAtLeast(lastPositionMs)
    }

    suspend fun onPlaybackStopped(userId: String) {
        scrobbleCurrentIfEligible(userId)
        currentKey = null
        currentRaw = null
    }

    private suspend fun scrobbleCurrentIfEligible(userId: String) {
        val raw = currentRaw ?: return
        val listenedMs = listenedDurationMs(raw)
        if (listenedMs < MIN_LISTEN_MS) return
        val threshold = scrobbleThresholdMs(raw.durationMs)
        if (listenedMs >= threshold) {
            repository.scrobble(userId, raw, listenedMs)
        }
    }

    private fun listenedDurationMs(raw: RawNowPlaying): Long {
        val elapsed = (System.currentTimeMillis() - startedAtMs).coerceAtLeast(0L)
        return maxOf(elapsed, lastPositionMs, raw.positionMs)
    }

    private fun scrobbleThresholdMs(durationMs: Long): Long {
        if (durationMs <= 0L) return SCROBBLE_MIN_MS
        return min((durationMs * SCROBBLE_RATIO).toLong(), SCROBBLE_MIN_MS)
            .coerceIn(MIN_LISTEN_MS, durationMs)
    }

    private fun trackKey(raw: RawNowPlaying): String =
        "${raw.artist.lowercase()}::${raw.track.lowercase()}"
}
