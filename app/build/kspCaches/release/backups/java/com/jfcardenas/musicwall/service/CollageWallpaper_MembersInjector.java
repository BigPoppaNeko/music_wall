package com.jfcardenas.musicwall.service;

import coil.ImageLoader;
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase;
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRendererFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class CollageWallpaper_MembersInjector implements MembersInjector<CollageWallpaper> {
  private final Provider<GetMusicImagesUseCase> getMusicImagesProvider;

  private final Provider<WallpaperRendererFactory> rendererFactoryProvider;

  private final Provider<ImageLoader> imageLoaderProvider;

  private CollageWallpaper_MembersInjector(Provider<GetMusicImagesUseCase> getMusicImagesProvider,
      Provider<WallpaperRendererFactory> rendererFactoryProvider,
      Provider<ImageLoader> imageLoaderProvider) {
    this.getMusicImagesProvider = getMusicImagesProvider;
    this.rendererFactoryProvider = rendererFactoryProvider;
    this.imageLoaderProvider = imageLoaderProvider;
  }

  @Override
  public void injectMembers(CollageWallpaper instance) {
    injectGetMusicImages(instance, getMusicImagesProvider.get());
    injectRendererFactory(instance, rendererFactoryProvider.get());
    injectImageLoader(instance, imageLoaderProvider.get());
  }

  public static MembersInjector<CollageWallpaper> create(
      Provider<GetMusicImagesUseCase> getMusicImagesProvider,
      Provider<WallpaperRendererFactory> rendererFactoryProvider,
      Provider<ImageLoader> imageLoaderProvider) {
    return new CollageWallpaper_MembersInjector(getMusicImagesProvider, rendererFactoryProvider, imageLoaderProvider);
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.CollageWallpaper.getMusicImages")
  public static void injectGetMusicImages(CollageWallpaper instance,
      GetMusicImagesUseCase getMusicImages) {
    instance.getMusicImages = getMusicImages;
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.CollageWallpaper.rendererFactory")
  public static void injectRendererFactory(CollageWallpaper instance,
      WallpaperRendererFactory rendererFactory) {
    instance.rendererFactory = rendererFactory;
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.CollageWallpaper.imageLoader")
  public static void injectImageLoader(CollageWallpaper instance, ImageLoader imageLoader) {
    instance.imageLoader = imageLoader;
  }
}
