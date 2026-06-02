package com.jfcardenas.musicwall.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_scrobbles",
    indices = [
        Index(value = ["userId", "playedAt"]),
        Index(value = ["userId", "artistName", "trackName"]),
    ],
)
data class LocalScrobbleEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val artistName: String,
    val trackName: String,
    val albumName: String,
    val imageUrl: String?,
    val artistMbid: String? = null,
    val trackMbid: String? = null,
    val albumMbid: String? = null,
    val matchConfidence: Float = 1f,
    val rawArtist: String = "",
    val rawTrack: String = "",
    val rawAlbum: String = "",
    val sourceApp: String? = null,
    val playedAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val isNowPlaying: Boolean = false,
)
