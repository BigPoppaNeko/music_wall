package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jfcardenas.musicwall.data.local.db.entity.VetoedAlbum

@Dao
interface VetoedAlbumDao {

    @Query("SELECT id FROM vetoed_albums")
    suspend fun getAllIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: VetoedAlbum)
}
