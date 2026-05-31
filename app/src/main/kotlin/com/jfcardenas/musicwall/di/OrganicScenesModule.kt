package com.jfcardenas.musicwall.di

import android.content.Context
import com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OrganicScenesModule {

    @Provides
    @Singleton
    @Named("scene_britrock")
    fun provideBritrockScene(@ApplicationContext ctx: Context): OrganicRenderer =
        OrganicRenderer(ctx, "e9c3f7a4-2b8d-4e6c-a1f5-d7b0c9e3f815")

    @Provides
    @Singleton
    @Named("scene_bano_bar_lima")
    fun provideBanoBarLimaScene(@ApplicationContext ctx: Context): OrganicRenderer =
        OrganicRenderer(ctx, "6dda3e32-c1bc-4801-bcb1-8bb5fd67bdaf")

    @Provides
    @Singleton
    @Named("scene_woodstock")
    fun provideWoodstockScene(@ApplicationContext ctx: Context): OrganicRenderer =
        OrganicRenderer(ctx, "155fc4ba-11fe-42be-ab07-47051a81344c", postProcess = true)
}
