package com.jfcardenas.musicwall.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mural_history")
data class MuralRecord(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val styleId: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
)
