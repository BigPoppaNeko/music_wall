package com.jfcardenas.musicwall.data.local.db.dao

import androidx.room.*
import com.jfcardenas.musicwall.data.local.db.entity.FavoriteAlbum

@Dao
interface FavoriteAlbumDao {

    @Query("SELECT * FROM favorite_albums ORDER BY savedAt DESC")
    suspend fun getAll(): List<FavoriteAlbum>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: FavoriteAlbum)

    @Query("DELETE FROM favorite_albums WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_albums WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean
}
