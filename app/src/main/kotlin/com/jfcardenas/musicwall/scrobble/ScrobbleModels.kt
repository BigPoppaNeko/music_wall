package com.jfcardenas.musicwall.scrobble

data class RawNowPlaying(
    val artist: String,
    val track: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val sourceApp: String? = null,
    val artworkUrl: String? = null,
)

data class ResolvedScrobble(
    val artist: String,
    val track: String,
    val album: String,
    val imageUrl: String?,
    val artistMbid: String? = null,
    val trackMbid: String? = null,
    val albumMbid: String? = null,
    val matchConfidence: Float = 1f,
    val rawArtist: String = "",
    val rawTrack: String = "",
    val rawAlbum: String = "",
)
