package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.*
import com.jfcardenas.musicwall.data.local.db.entity.MuralRecord

@Dao
interface MuralDao {
    @Query("SELECT * FROM mural_history ORDER BY createdAt DESC")
    suspend fun getAll(): List<MuralRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mural: MuralRecord)

    @Delete
    suspend fun delete(mural: MuralRecord)

    @Query("SELECT COUNT(*) FROM mural_history")
    suspend fun count(): Int
}
