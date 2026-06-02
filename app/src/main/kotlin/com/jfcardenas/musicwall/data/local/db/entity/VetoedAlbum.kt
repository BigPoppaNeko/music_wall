package com.jfcardenas.musicwall.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vetoed_albums")
data class VetoedAlbum(
    @PrimaryKey val id: String,
    val albumName: String,
    val artistName: String,
    val vetoedAt: Long = System.currentTimeMillis(),
)
