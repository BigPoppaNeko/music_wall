package com.jfcardenas.musicwall.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.jfcardenas.musicwall.api.LastFmApiException
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.getExtraLargeUrl
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao
import com.jfcardenas.musicwall.data.local.db.dao.ArtistDao
import com.jfcardenas.musicwall.data.local.db.dao.TrackDao
import com.jfcardenas.musicwall.data.local.db.entity.AlbumEntity
import com.jfcardenas.musicwall.data.local.db.entity.ArtistEntity
import com.jfcardenas.musicwall.data.local.db.entity.TrackEntity
import com.jfcardenas.musicwall.domain.model.MusicImage
import com.jfcardenas.musicwall.domain.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LastFmRepository"
private const val CACHE_TTL_MS = 3_600_000L

// Fixed cache keys for sources that don't use a period parameter
private const val CACHE_KEY_LOVED = "loved"
private const val CACHE_KEY_WEEKLY = "weekly_current"

@Singleton
class LastFmRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val service: LastFmService,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val trackDao: TrackDao
) : MusicRepository {

    override suspend fun getImages(
        username: String,
        imageKind: String,
        period: String,
        limit: Int,
        forceRefresh: Boolean
    ): NetworkResult<List<MusicImage>> {

        val cacheKey = cacheKeyFor(imageKind, period)

        if (!isNetworkAvailable()) {
            val cached = getCachedImages(username, imageKind, cacheKey, limit)
            if (cached.isNotEmpty()) {
                Log.d(TAG, "Offline — serving ${cached.size} cached images")
                return NetworkResult.Success(cached)
            }
            return NetworkResult.Error(ErrorType.NO_INTERNET, "Sin conexión a Internet")
        }

        if (!forceRefresh && isCacheValid(username, imageKind, cacheKey)) {
            val cached = getCachedImages(username, imageKind, cacheKey, limit)
            if (cached.isNotEmpty()) {
                Log.d(TAG, "Cache hit for $username [$imageKind/$cacheKey] — ${cached.size} images")
                return NetworkResult.Success(cached)
            }
        }

        return try {
            Log.d(TAG, "Fetching $imageKind for $username [period=$period, limit=$limit]")
            val images = fetchFromApi(username, imageKind, period, limit)

            if (images.isEmpty()) {
                return NetworkResult.Error(ErrorType.EMPTY_RESPONSE, "No hay imágenes para este período")
            }

            persistToCache(username, imageKind, cacheKey, images)
            Log.d(TAG, "Fetched and cached ${images.size} images for $username")
            NetworkResult.Success(images)

        } catch (e: LastFmApiException) {
            Log.e(TAG, "Last.fm API error (code=${e.code}): ${e.message}")
            val errorType = when (e.code) {
                6 -> ErrorType.USER_NOT_FOUND
                10 -> ErrorType.API_KEY_INVALID
                11, 16 -> ErrorType.SERVER_ERROR
                29 -> ErrorType.RATE_LIMIT
                else -> ErrorType.UNKNOWN
            }
            NetworkResult.Error(errorType, e.message ?: "Error de API")
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP ${e.code()}: ${e.message()}")
            NetworkResult.Error(
                if (e.code() in 500..599) ErrorType.SERVER_ERROR else ErrorType.UNKNOWN,
                "Error HTTP ${e.code()}"
            )
        } catch (e: IOException) {
            Log.e(TAG, "Network error: ${e.message}")
            val cached = getCachedImages(username, imageKind, cacheKey, limit)
            if (cached.isNotEmpty()) {
                Log.d(TAG, "Network error — fallback to ${cached.size} cached images")
                return NetworkResult.Success(cached)
            }
            NetworkResult.Error(ErrorType.NO_INTERNET, "Error de red: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            NetworkResult.Error(ErrorType.UNKNOWN, "Error inesperado: ${e.message}")
        }
    }

    override suspend fun clearCache(username: String?) {
        val cutoff = System.currentTimeMillis()
        albumDao.deleteOlderThan(cutoff)
        artistDao.deleteOlderThan(cutoff)
        trackDao.deleteOlderThan(cutoff)
        Log.d(TAG, "Cache cleared for ${username ?: "all users"}")
    }

    // ── API fetch ─────────────────────────────────────────────────────────────

    private suspend fun fetchFromApi(
        username: String, imageKind: String, period: String, limit: Int
    ): List<MusicImage> = when (imageKind) {

        "ALBUMS" -> service.getTopAlbums(user = username, period = period, limit = limit)
            .topAlbums.albums
            .mapIndexedNotNull { index, album ->
                album.images.getExtraLargeUrl()?.let { url ->
                    MusicImage(
                        url = url,
                        name = album.name,
                        artistName = album.artist.name,
                        rank = index + 1,
                        kind = MusicImage.Kind.ALBUM,
                        playcount = album.playcount?.toIntOrNull() ?: 0,
                        mbid = album.mbid?.takeIf { it.isNotEmpty() },
                        lastFmUrl = album.url?.takeIf { it.isNotEmpty() }
                    )
                }
            }

        "ARTISTS" -> service.getTopArtists(user = username, period = period, limit = limit)
            .topArtists.artists
            .mapIndexedNotNull { index, artist ->
                artist.images?.getExtraLargeUrl()?.let { url ->
                    MusicImage(
                        url = url,
                        name = artist.name,
                        artistName = artist.name,
                        rank = index + 1,
                        kind = MusicImage.Kind.ARTIST,
                        playcount = artist.playcount?.toIntOrNull() ?: 0,
                        mbid = artist.mbid?.takeIf { it.isNotEmpty() },
                        lastFmUrl = artist.url?.takeIf { it.isNotEmpty() }
                    )
                }
            }

        "TRACKS" -> service.getTopTracks(user = username, period = period, limit = limit)
            .topTracks.tracks
            .mapIndexedNotNull { index, track ->
                track.images.getExtraLargeUrl()?.let { url ->
                    MusicImage(
                        url = url,
                        name = track.name,
                        artistName = track.artist.name,
                        rank = track.attr?.rank?.toIntOrNull() ?: (index + 1),
                        kind = MusicImage.Kind.TRACK,
                        playcount = track.playcount?.toIntOrNull() ?: 0,
                        mbid = track.mbid?.takeIf { it.isNotEmpty() },
                        lastFmUrl = track.url?.takeIf { it.isNotEmpty() }
                    )
                }
            }

        "LOVED" -> service.getLovedTracks(user = username, limit = limit)
            .lovedTracks.tracks
            .mapIndexedNotNull { index, track ->
                track.images.getExtraLargeUrl()?.let { url ->
                    MusicImage(
                        url = url,
                        name = track.name,
                        artistName = track.artist.name,
                        rank = index + 1,
                        kind = MusicImage.Kind.LOVED_TRACK,
                        mbid = track.mbid?.takeIf { it.isNotEmpty() },
                        lastFmUrl = track.url?.takeIf { it.isNotEmpty() },
                        timestampUts = track.date?.uts?.toLongOrNull()
                    )
                }
            }

        "WEEKLY_ALBUMS" -> service.getWeeklyAlbumChart(user = username)
            .chart.albums
            .take(limit)
            .mapIndexedNotNull { index, album ->
                album.images?.getExtraLargeUrl()?.let { url ->
                    MusicImage(
                        url = url,
                        name = album.name,
                        artistName = album.artist.name,
                        rank = index + 1,
                        kind = MusicImage.Kind.WEEKLY_ALBUM,
                        playcount = album.playcount?.toIntOrNull() ?: 0,
                        mbid = album.mbid?.takeIf { it.isNotEmpty() },
                        lastFmUrl = album.url?.takeIf { it.isNotEmpty() }
                    )
                }
            }

        "WEEKLY_ARTISTS" -> service.getWeeklyArtistChart(user = username)
            .chart.artists
            .take(limit)
            .mapIndexedNotNull { index, artist ->
                artist.images?.getExtraLargeUrl()?.let { url ->
                    MusicImage(
                        url = url,
                        name = artist.name,
                        artistName = artist.name,
                        rank = index + 1,
                        kind = MusicImage.Kind.WEEKLY_ARTIST,
                        playcount = artist.playcount?.toIntOrNull() ?: 0,
                        mbid = artist.mbid?.takeIf { it.isNotEmpty() },
                        lastFmUrl = artist.url?.takeIf { it.isNotEmpty() }
                    )
                }
            }

        else -> emptyList()
    }

    // ── Cache read ────────────────────────────────────────────────────────────

    private suspend fun getCachedImages(
        username: String, imageKind: String, cacheKey: String, limit: Int
    ): List<MusicImage> = when (imageKind) {

        "ALBUMS", "WEEKLY_ALBUMS" -> {
            val kind = if (imageKind == "ALBUMS") MusicImage.Kind.ALBUM else MusicImage.Kind.WEEKLY_ALBUM
            albumDao.get(username, cacheKey, limit).map { e ->
                MusicImage(e.imageUrl, e.albumName, e.artistName, e.rank, kind, e.playcount, e.mbid, e.lastFmUrl)
            }
        }

        "ARTISTS", "WEEKLY_ARTISTS" -> {
            val kind = if (imageKind == "ARTISTS") MusicImage.Kind.ARTIST else MusicImage.Kind.WEEKLY_ARTIST
            artistDao.get(username, cacheKey, limit).map { e ->
                MusicImage(e.imageUrl, e.artistName, e.artistName, e.rank, kind, e.playcount, e.mbid, e.lastFmUrl)
            }
        }

        "TRACKS" -> trackDao.get(username, cacheKey, "TOP", limit).map { e ->
            MusicImage(e.imageUrl, e.trackName, e.artistName, e.rank, MusicImage.Kind.TRACK, e.playcount, e.mbid, e.lastFmUrl)
        }

        "LOVED" -> trackDao.get(username, cacheKey, "LOVED", limit).map { e ->
            MusicImage(e.imageUrl, e.trackName, e.artistName, e.rank, MusicImage.Kind.LOVED_TRACK, 0, e.mbid, e.lastFmUrl)
        }

        else -> emptyList()
    }

    // ── Cache write ───────────────────────────────────────────────────────────

    private suspend fun persistToCache(
        username: String, imageKind: String, cacheKey: String, images: List<MusicImage>
    ) {
        when (imageKind) {

            "ALBUMS", "WEEKLY_ALBUMS" -> {
                albumDao.deleteForUserPeriod(username, cacheKey)
                albumDao.insertAll(images.map { img ->
                    AlbumEntity(
                        id = "${username}_${cacheKey}_${img.name}_${img.artistName}",
                        username = username, period = cacheKey,
                        albumName = img.name, artistName = img.artistName,
                        imageUrl = img.url, rank = img.rank,
                        playcount = img.playcount, mbid = img.mbid, lastFmUrl = img.lastFmUrl
                    )
                })
            }

            "ARTISTS", "WEEKLY_ARTISTS" -> {
                artistDao.deleteForUserPeriod(username, cacheKey)
                artistDao.insertAll(images.map { img ->
                    ArtistEntity(
                        id = "${username}_${cacheKey}_${img.name}",
                        username = username, period = cacheKey,
                        artistName = img.name, imageUrl = img.url, rank = img.rank,
                        playcount = img.playcount, mbid = img.mbid, lastFmUrl = img.lastFmUrl
                    )
                })
            }

            "TRACKS" -> {
                trackDao.deleteForUserPeriodKind(username, cacheKey, "TOP")
                trackDao.insertAll(images.map { img ->
                    TrackEntity(
                        id = "${username}_${cacheKey}_TOP_${img.name}_${img.artistName}",
                        username = username, period = cacheKey, kind = "TOP",
                        trackName = img.name, artistName = img.artistName, albumName = "",
                        imageUrl = img.url, rank = img.rank,
                        playcount = img.playcount, mbid = img.mbid, lastFmUrl = img.lastFmUrl
                    )
                })
            }

            "LOVED" -> {
                trackDao.deleteForUserPeriodKind(username, cacheKey, "LOVED")
                trackDao.insertAll(images.map { img ->
                    TrackEntity(
                        id = "${username}_${cacheKey}_LOVED_${img.name}_${img.artistName}",
                        username = username, period = cacheKey, kind = "LOVED",
                        trackName = img.name, artistName = img.artistName, albumName = "",
                        imageUrl = img.url, rank = img.rank,
                        mbid = img.mbid, lastFmUrl = img.lastFmUrl
                    )
                })
            }
        }
    }

    // ── Cache validity ────────────────────────────────────────────────────────

    private suspend fun isCacheValid(username: String, imageKind: String, cacheKey: String): Boolean {
        val oldest = when (imageKind) {
            "ALBUMS", "WEEKLY_ALBUMS" -> albumDao.oldestFetchTime(username, cacheKey)
            "ARTISTS", "WEEKLY_ARTISTS" -> artistDao.oldestFetchTime(username, cacheKey)
            "TRACKS" -> trackDao.oldestFetchTime(username, cacheKey, "TOP")
            "LOVED" -> trackDao.oldestFetchTime(username, cacheKey, "LOVED")
            else -> null
        }
        return oldest != null && System.currentTimeMillis() - oldest < CACHE_TTL_MS
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun cacheKeyFor(imageKind: String, period: String): String = when (imageKind) {
        "LOVED" -> CACHE_KEY_LOVED
        "WEEKLY_ALBUMS", "WEEKLY_ARTISTS" -> CACHE_KEY_WEEKLY
        else -> period
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
