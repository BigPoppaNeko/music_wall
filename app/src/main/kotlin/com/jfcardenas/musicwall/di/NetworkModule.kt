package com.jfcardenas.musicwall.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.jfcardenas.musicwall.api.LastFmService
import com.jfcardenas.musicwall.api.LrcLibService
import com.jfcardenas.musicwall.api.DeezerService
import com.jfcardenas.musicwall.api.DiscogsService
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLastFmService(): LastFmService = createLastFmService()

    @Provides
    @Singleton
    fun provideLyricsService(): LyricsService = createLyricsService()

    @Provides
    @Singleton
    fun provideLrcLibService(): LrcLibService = createLrcLibService()

    @Provides
    @Singleton
    fun provideDiscogsService(): DiscogsService = createDiscogsService()

    @Provides
    @Singleton
    fun provideDeezerService(): DeezerService = createDeezerService()

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.15) // 15% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB disk cache
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
}
