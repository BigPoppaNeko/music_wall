package com.jfcardenas.musicwall.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jfcardenas.musicwall.data.local.db.dao.LfMatchCacheDao
import com.jfcardenas.musicwall.data.local.db.dao.LocalScrobbleDao
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao
import com.jfcardenas.musicwall.data.local.db.dao.ArtistDao
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao
import com.jfcardenas.musicwall.data.local.db.dao.MuralDao
import com.jfcardenas.musicwall.data.local.db.dao.TrackDao
import com.jfcardenas.musicwall.data.local.db.dao.VetoedAlbumDao
import com.jfcardenas.musicwall.data.local.db.entity.LfMatchCacheEntity
import com.jfcardenas.musicwall.data.local.db.entity.LocalScrobbleEntity
import com.jfcardenas.musicwall.data.local.db.entity.AlbumEntity
import com.jfcardenas.musicwall.data.local.db.entity.ArtistEntity
import com.jfcardenas.musicwall.data.local.db.entity.FavoriteAlbum
import com.jfcardenas.musicwall.data.local.db.entity.MuralRecord
import com.jfcardenas.musicwall.data.local.db.entity.TrackEntity
import com.jfcardenas.musicwall.data.local.db.entity.VetoedAlbum

@Database(
    entities = [
        AlbumEntity::class, ArtistEntity::class, TrackEntity::class, MuralRecord::class,
        FavoriteAlbum::class, VetoedAlbum::class, LocalScrobbleEntity::class, LfMatchCacheEntity::class,
    ],
    version = 6,
    exportSchema = false
)
abstract class MusicWallDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun trackDao(): TrackDao
    abstract fun muralDao(): MuralDao
    abstract fun favoriteAlbumDao(): FavoriteAlbumDao
    abstract fun vetoedAlbumDao(): VetoedAlbumDao
    abstract fun localScrobbleDao(): LocalScrobbleDao
    abstract fun lfMatchCacheDao(): LfMatchCacheDao

    companion object {
        fun create(context: Context): MusicWallDatabase =
            Room.databaseBuilder(context, MusicWallDatabase::class.java, "musicwall.db")
                .addMigrations(*MusicWallMigrations.ALL)
                .fallbackToDestructiveMigrationFrom(1, 2, 3)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}
