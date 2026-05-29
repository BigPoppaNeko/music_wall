package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfcardenas.musicwall.data.local.db.entity.ArtistEntity

@Dao
interface ArtistDao {

    @Query("SELECT * FROM artists WHERE username = :username AND period = :period ORDER BY rank ASC LIMIT :limit")
    suspend fun get(username: String, period: String, limit: Int): List<ArtistEntity>

    @Query("SELECT MIN(fetchedAt) FROM artists WHERE username = :username AND period = :period")
    suspend fun oldestFetchTime(username: String, period: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(artists: List<ArtistEntity>)

    @Query("DELETE FROM artists WHERE username = :username AND period = :period")
    suspend fun deleteForUserPeriod(username: String, period: String)

    @Query("DELETE FROM artists WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
