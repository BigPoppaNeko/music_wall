package com.jfcardenas.musicwall.di

import com.jfcardenas.musicwall.data.LastFmRepository
import com.jfcardenas.musicwall.domain.repository.MusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: LastFmRepository): MusicRepository
}
