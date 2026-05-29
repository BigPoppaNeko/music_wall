package com.jfcardenas.musicwall.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    indices = [Index(value = ["username", "period", "kind"], name = "idx_tracks_user_period_kind")]
)
data class TrackEntity(
    @PrimaryKey val id: String,     // "${username}_${period}_${kind}_${trackName}_${artistName}"
    val username: String,
    val period: String,             // period string for top tracks; "loved" for loved tracks
    val kind: String,               // "TOP" or "LOVED"
    val trackName: String,
    val artistName: String,
    val albumName: String,
    val imageUrl: String,
    val rank: Int,
    val playcount: Int = 0,
    val mbid: String? = null,
    val lastFmUrl: String? = null,
    val fetchedAt: Long = System.currentTimeMillis()
)
