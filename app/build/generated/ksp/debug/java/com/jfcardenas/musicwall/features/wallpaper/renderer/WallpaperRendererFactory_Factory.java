package com.jfcardenas.musicwall.features.wallpaper.renderer;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class WallpaperRendererFactory_Factory implements Factory<WallpaperRendererFactory> {
  private final Provider<AlbumWallRenderer> albumWallRendererProvider;

  private WallpaperRendererFactory_Factory(Provider<AlbumWallRenderer> albumWallRendererProvider) {
    this.albumWallRendererProvider = albumWallRendererProvider;
  }

  @Override
  public WallpaperRendererFactory get() {
    return newInstance(albumWallRendererProvider.get());
  }

  public static WallpaperRendererFactory_Factory create(
      Provider<AlbumWallRenderer> albumWallRendererProvider) {
    return new WallpaperRendererFactory_Factory(albumWallRendererProvider);
  }

  public static WallpaperRendererFactory newInstance(AlbumWallRenderer albumWallRenderer) {
    return new WallpaperRendererFactory(albumWallRenderer);
  }
}
