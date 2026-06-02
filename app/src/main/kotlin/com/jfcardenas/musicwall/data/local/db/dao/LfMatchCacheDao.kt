package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfcardenas.musicwall.data.local.db.entity.LfMatchCacheEntity

@Dao
interface LfMatchCacheDao {

    @Query("SELECT * FROM lf_match_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): LfMatchCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LfMatchCacheEntity)

    @Query("DELETE FROM lf_match_cache WHERE cachedAt < :beforeMs")
    suspend fun evictOlderThan(beforeMs: Long)
}
