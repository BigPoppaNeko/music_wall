package com.jfcardenas.musicwall.scrobble

import com.jfcardenas.musicwall.data.local.db.dao.LocalScrobbleDao
import com.jfcardenas.musicwall.data.local.db.entity.LocalScrobbleEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScrobbleRepository @Inject constructor(
    private val localScrobbleDao: LocalScrobbleDao,
    private val resolver: LastFmTrackResolver,
) {
    fun observeRecent(userId: String, limit: Int = 30): Flow<List<LocalScrobbleEntity>> =
        localScrobbleDao.observeRecent(userId, limit)

    suspend fun getRecent(userId: String, limit: Int = 30): List<LocalScrobbleEntity> =
        localScrobbleDao.getRecent(userId, limit)

    suspend fun getNowPlaying(userId: String): LocalScrobbleEntity? =
        localScrobbleDao.getNowPlaying(userId)

    suspend fun updateNowPlaying(userId: String, raw: RawNowPlaying) {
        val resolved = resolver.resolve(raw) ?: return
        localScrobbleDao.clearNowPlaying(userId)
        localScrobbleDao.insert(
            LocalScrobbleEntity(
                id = "np_${userId}_${System.currentTimeMillis()}",
                userId = userId,
                artistName = resolved.artist,
                trackName = resolved.track,
                albumName = resolved.album,
                imageUrl = resolved.imageUrl,
                artistMbid = resolved.artistMbid,
                trackMbid = resolved.trackMbid,
                albumMbid = resolved.albumMbid,
                matchConfidence = resolved.matchConfidence,
                rawArtist = resolved.rawArtist,
                rawTrack = resolved.rawTrack,
                rawAlbum = resolved.rawAlbum,
                sourceApp = raw.sourceApp,
                playedAt = System.currentTimeMillis(),
                durationMs = raw.durationMs,
                isNowPlaying = true,
            ),
        )
    }

    suspend fun scrobble(userId: String, raw: RawNowPlaying, listenedMs: Long) {
        val resolved = resolver.resolve(raw) ?: return
        localScrobbleDao.clearNowPlaying(userId)
        localScrobbleDao.insert(
            LocalScrobbleEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                artistName = resolved.artist,
                trackName = resolved.track,
                albumName = resolved.album,
                imageUrl = resolved.imageUrl,
                artistMbid = resolved.artistMbid,
                trackMbid = resolved.trackMbid,
                albumMbid = resolved.albumMbid,
                matchConfidence = resolved.matchConfidence,
                rawArtist = resolved.rawArtist,
                rawTrack = resolved.rawTrack,
                rawAlbum = resolved.rawAlbum,
                sourceApp = raw.sourceApp,
                playedAt = System.currentTimeMillis(),
                durationMs = listenedMs.coerceAtLeast(0L),
                isNowPlaying = false,
            ),
        )
    }

    suspend fun getWeekAlbumPlays(userId: String): Map<String, Int> {
        val since = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return localScrobbleDao.getSince(userId, since)
            .groupBy { "${it.artistName}::${it.albumName}".lowercase() }
            .mapValues { (_, entries) -> entries.size }
    }
}
