package com.jfcardenas.musicwall.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lf_match_cache")
data class LfMatchCacheEntity(
    @PrimaryKey val cacheKey: String,
    val artistName: String,
    val trackName: String,
    val albumName: String,
    val imageUrl: String?,
    val artistMbid: String? = null,
    val trackMbid: String? = null,
    val albumMbid: String? = null,
    val matchConfidence: Float = 1f,
    val cachedAt: Long = System.currentTimeMillis(),
)
