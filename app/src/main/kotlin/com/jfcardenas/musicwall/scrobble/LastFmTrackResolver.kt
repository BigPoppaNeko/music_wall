package com.jfcardenas.musicwall.scrobble

import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.local.db.dao.LfMatchCacheDao
import com.jfcardenas.musicwall.data.local.db.entity.LfMatchCacheEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class LastFmTrackResolver @Inject constructor(
    private val lastFmService: LastFmService,
    private val matchCacheDao: LfMatchCacheDao,
) {
    companion object {
        private const val CACHE_TTL_MS = 30L * 24 * 60 * 60 * 1000
        private const val MIN_CONFIDENCE = 0.65f
    }

    suspend fun resolve(raw: RawNowPlaying): ResolvedScrobble? {
        val normalized = MetadataNormalizer.normalize(raw) ?: return null
        val key = MetadataNormalizer.cacheKey(normalized.artist, normalized.track, normalized.album)

        matchCacheDao.get(key)?.takeIf { System.currentTimeMillis() - it.cachedAt < CACHE_TTL_MS }
            ?.let { return it.toResolved(normalized) }

        val resolved = resolveFromApi(normalized) ?: return fallback(normalized)
        if (resolved.matchConfidence < MIN_CONFIDENCE) return fallback(normalized)

        matchCacheDao.insert(
            LfMatchCacheEntity(
                cacheKey = key,
                artistName = resolved.artist,
                trackName = resolved.track,
                albumName = resolved.album,
                imageUrl = resolved.imageUrl,
                artistMbid = resolved.artistMbid,
                trackMbid = resolved.trackMbid,
                albumMbid = resolved.albumMbid,
                matchConfidence = resolved.matchConfidence,
            ),
        )
        return resolved
    }

    private suspend fun resolveFromApi(raw: RawNowPlaying): ResolvedScrobble? {
        tryDirectTrackInfo(raw)?.let { return it }

        val artist = resolveArtistName(raw.artist) ?: raw.artist
        tryDirectTrackInfo(raw.copy(artist = artist))?.let { return it }

        return searchTrack(raw.copy(artist = artist))
    }

    private suspend fun tryDirectTrackInfo(raw: RawNowPlaying): ResolvedScrobble? = try {
        val info = lastFmService.getTrackInfo(artist = raw.artist, track = raw.track).track ?: return null
        val albumTitle = info.album?.title?.takeIf { it.isNotBlank() } ?: raw.album
        val imageUrl = info.album?.images?.getExtraLargeUrl() ?: raw.artworkUrl
        ResolvedScrobble(
            artist = info.artist.name.ifBlank { raw.artist },
            track = info.name.ifBlank { raw.track },
            album = albumTitle,
            imageUrl = imageUrl,
            artistMbid = info.artist.mbid?.takeIf { it.isNotBlank() },
            trackMbid = null,
            albumMbid = info.album?.mbid?.takeIf { it.isNotBlank() },
            matchConfidence = scoreMatch(raw.artist, raw.track, info.artist.name, info.name),
            rawArtist = raw.artist,
            rawTrack = raw.track,
            rawAlbum = raw.album,
        )
    } catch (_: Exception) {
        null
    }

    private suspend fun searchTrack(raw: RawNowPlaying): ResolvedScrobble? {
        return try {
            val matches = lastFmService.searchTracks(track = raw.track, artist = raw.artist, limit = 5)
                .results.trackMatches.tracks
            val best = matches.maxByOrNull { candidate ->
                scoreMatch(raw.artist, raw.track, candidate.artist, candidate.name)
            } ?: return null
            val score = scoreMatch(raw.artist, raw.track, best.artist, best.name)
            if (score < MIN_CONFIDENCE) return null

            val detail = try {
                lastFmService.getTrackInfo(artist = best.artist, track = best.name).track
            } catch (_: Exception) {
                null
            }
            ResolvedScrobble(
                artist = detail?.artist?.name ?: best.artist,
                track = detail?.name ?: best.name,
                album = detail?.album?.title?.takeIf { it.isNotBlank() } ?: raw.album,
                imageUrl = detail?.album?.images?.getExtraLargeUrl() ?: raw.artworkUrl,
                artistMbid = detail?.artist?.mbid?.takeIf { it.isNotBlank() },
                trackMbid = best.mbid?.takeIf { it.isNotBlank() },
                albumMbid = detail?.album?.mbid?.takeIf { it.isNotBlank() },
                matchConfidence = score,
                rawArtist = raw.artist,
                rawTrack = raw.track,
                rawAlbum = raw.album,
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun resolveArtistName(artist: String): String? = try {
        val matches = lastFmService.searchArtists(artist = artist, limit = 5)
            .results.artistMatches.artists
        matches.maxByOrNull { it.listeners?.toLongOrNull() ?: 0L }?.name
    } catch (_: Exception) {
        null
    }

    private fun fallback(raw: RawNowPlaying): ResolvedScrobble =
        ResolvedScrobble(
            artist = raw.artist,
            track = raw.track,
            album = raw.album,
            imageUrl = raw.artworkUrl,
            matchConfidence = 0.5f,
            rawArtist = raw.artist,
            rawTrack = raw.track,
            rawAlbum = raw.album,
        )

    private fun scoreMatch(rawArtist: String, rawTrack: String, candidateArtist: String, candidateTrack: String): Float {
        val artistScore = stringSimilarity(rawArtist, candidateArtist)
        val trackScore = stringSimilarity(rawTrack, candidateTrack)
        return min(1f, artistScore * 0.45f + trackScore * 0.55f)
    }

    private fun stringSimilarity(a: String, b: String): Float {
        val left = a.trim().lowercase()
        val right = b.trim().lowercase()
        if (left == right) return 1f
        if (left.isEmpty() || right.isEmpty()) return 0f
        if (left.contains(right) || right.contains(left)) return 0.85f
        val distance = levenshtein(left, right)
        val maxLen = maxOf(left.length, right.length).coerceAtLeast(1)
        return (1f - distance.toFloat() / maxLen).coerceIn(0f, 1f)
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val costs = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = i - 1
            costs[0] = i
            for (j in 1..b.length) {
                val tmp = costs[j]
                costs[j] = min(
                    min(costs[j] + 1, costs[j - 1] + 1),
                    prev + if (a[i - 1] == b[j - 1]) 0 else 1,
                )
                prev = tmp
            }
        }
        return costs[b.length]
    }

    private fun LfMatchCacheEntity.toResolved(raw: RawNowPlaying) = ResolvedScrobble(
        artist = artistName,
        track = trackName,
        album = albumName.ifBlank { raw.album },
        imageUrl = imageUrl ?: raw.artworkUrl,
        artistMbid = artistMbid,
        trackMbid = trackMbid,
        albumMbid = albumMbid,
        matchConfidence = matchConfidence,
        rawArtist = raw.artist,
        rawTrack = raw.track,
        rawAlbum = raw.album,
    )
}
