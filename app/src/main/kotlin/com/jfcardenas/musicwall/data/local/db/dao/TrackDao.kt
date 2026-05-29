package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfcardenas.musicwall.data.local.db.entity.TrackEntity

@Dao
interface TrackDao {

    @Query("SELECT * FROM tracks WHERE username = :username AND period = :period AND kind = :kind ORDER BY rank ASC LIMIT :limit")
    suspend fun get(username: String, period: String, kind: String, limit: Int): List<TrackEntity>

    @Query("SELECT MIN(fetchedAt) FROM tracks WHERE username = :username AND period = :period AND kind = :kind")
    suspend fun oldestFetchTime(username: String, period: String, kind: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks WHERE username = :username AND period = :period AND kind = :kind")
    suspend fun deleteForUserPeriodKind(username: String, period: String, kind: String)

    @Query("DELETE FROM tracks WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
