package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfcardenas.musicwall.data.local.db.entity.LocalScrobbleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalScrobbleDao {

    @Query("SELECT * FROM local_scrobbles WHERE userId = :userId ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecent(userId: String, limit: Int = 30): List<LocalScrobbleEntity>

    @Query("SELECT * FROM local_scrobbles WHERE userId = :userId ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(userId: String, limit: Int = 30): Flow<List<LocalScrobbleEntity>>

    @Query("SELECT * FROM local_scrobbles WHERE userId = :userId AND isNowPlaying = 1 LIMIT 1")
    suspend fun getNowPlaying(userId: String): LocalScrobbleEntity?

    @Query("UPDATE local_scrobbles SET isNowPlaying = 0 WHERE userId = :userId")
    suspend fun clearNowPlaying(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scrobble: LocalScrobbleEntity)

    @Query("SELECT COUNT(*) FROM local_scrobbles WHERE userId = :userId")
    suspend fun count(userId: String): Int

    @Query(
        """
        SELECT * FROM local_scrobbles
        WHERE userId = :userId AND playedAt >= :sinceMs
        ORDER BY playedAt DESC
        """,
    )
    suspend fun getSince(userId: String, sinceMs: Long): List<LocalScrobbleEntity>
}
