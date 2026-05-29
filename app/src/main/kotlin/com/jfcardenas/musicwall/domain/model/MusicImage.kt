package com.jfcardenas.musicwall.domain.model

data class MusicImage(
    val url: String,
    val name: String,
    val artistName: String,
    val rank: Int,
    val kind: Kind,
    val playcount: Int = 0,
    val mbid: String? = null,
    val lastFmUrl: String? = null,
    val timestampUts: Long? = null
) {
    enum class Kind {
        ALBUM,
        ARTIST,
        TRACK,
        LOVED_TRACK,
        WEEKLY_ALBUM,
        WEEKLY_ARTIST
    }
}
