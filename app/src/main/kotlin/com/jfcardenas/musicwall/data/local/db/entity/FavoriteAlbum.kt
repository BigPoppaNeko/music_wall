package com.jfcardenas.musicwall.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_albums")
data class FavoriteAlbum(
    @PrimaryKey val id: String,          // "${artistName}::${albumName}"
    val albumName: String,
    val artistName: String,
    val imageUrl: String?,
    val mbid: String? = null,
    val savedAt: Long = System.currentTimeMillis(),
)
