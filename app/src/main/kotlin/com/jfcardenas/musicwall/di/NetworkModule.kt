package com.jfcardenas.musicwall.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.jfcardenas.musicwall.api.DeezerService
import com.jfcardenas.musicwall.api.DiscogsService
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.LrcLibService
import com.jfcardenas.musicwall.api.LyricsService
import com.jfcardenas.musicwall.api.createDeezerService
import com.jfcardenas.musicwall.api.createDiscogsService
import com.jfcardenas.musicwall.api.createLastFmService
import com.jfcardenas.musicwall.api.createLrcLibService
import com.jfcardenas.musicwall.api.createLyricsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLastFmService(httpClient: OkHttpClient): LastFmService =
        createLastFmService(httpClient)

    @Provides
    @Singleton
    fun provideLyricsService(httpClient: OkHttpClient): LyricsService =
        createLyricsService(httpClient)

    @Provides
    @Singleton
    fun provideLrcLibService(httpClient: OkHttpClient): LrcLibService =
        createLrcLibService(httpClient)

    @Provides
    @Singleton
    fun provideDiscogsService(httpClient: OkHttpClient): DiscogsService =
        createDiscogsService(httpClient)

    @Provides
    @Singleton
    fun provideDeezerService(httpClient: OkHttpClient): DeezerService =
        createDeezerService(httpClient)

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
}
