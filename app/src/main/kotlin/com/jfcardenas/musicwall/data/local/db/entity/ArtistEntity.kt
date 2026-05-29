package com.jfcardenas.musicwall.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    indices = [Index(value = ["username", "period"], name = "idx_artists_user_period")]
)
data class ArtistEntity(
    @PrimaryKey val id: String,
    val username: String,
    val period: String,
    val artistName: String,
    val imageUrl: String,
    val rank: Int,
    val playcount: Int = 0,
    val mbid: String? = null,
    val lastFmUrl: String? = null,
    val fetchedAt: Long = System.currentTimeMillis()
)
