package com.jfcardenas.musicwall.di

import android.content.Context
import com.jfcardenas.musicwall.data.local.db.MusicWallDatabase
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao
import com.jfcardenas.musicwall.data.local.db.dao.ArtistDao
import com.jfcardenas.musicwall.data.local.db.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicWallDatabase =
        MusicWallDatabase.create(context)

    @Provides
    fun provideAlbumDao(db: MusicWallDatabase): AlbumDao = db.albumDao()

    @Provides
    fun provideArtistDao(db: MusicWallDatabase): ArtistDao = db.artistDao()

    @Provides
    fun provideTrackDao(db: MusicWallDatabase): TrackDao = db.trackDao()
}
