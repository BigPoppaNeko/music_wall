package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfcardenas.musicwall.data.local.db.entity.AlbumEntity

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums WHERE username = :username AND period = :period ORDER BY rank ASC LIMIT :limit")
    suspend fun get(username: String, period: String, limit: Int): List<AlbumEntity>

    @Query("SELECT MIN(fetchedAt) FROM albums WHERE username = :username AND period = :period")
    suspend fun oldestFetchTime(username: String, period: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums WHERE username = :username AND period = :period")
    suspend fun deleteForUserPeriod(username: String, period: String)

    @Query("DELETE FROM albums WHERE fetchedAt < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("SELECT * FROM albums WHERE imageUrl != '' ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWithImage(limit: Int): List<AlbumEntity>

    @Query("UPDATE albums SET imageUrl = :imageUrl WHERE albumName = :albumName AND artistName = :artistName AND imageUrl = ''")
    suspend fun updateImageUrlIfEmpty(albumName: String, artistName: String, imageUrl: String)
}
