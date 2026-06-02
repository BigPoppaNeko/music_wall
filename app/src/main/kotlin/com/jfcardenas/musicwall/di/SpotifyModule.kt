package com.jfcardenas.musicwall.di

import com.jfcardenas.musicwall.api.SpotifyOEmbedService
import com.jfcardenas.musicwall.api.createSpotifyOEmbedService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SpotifyModule {

    @Provides
    @Singleton
    fun provideSpotifyOEmbedService(httpClient: OkHttpClient): SpotifyOEmbedService =
        createSpotifyOEmbedService(httpClient)
}
