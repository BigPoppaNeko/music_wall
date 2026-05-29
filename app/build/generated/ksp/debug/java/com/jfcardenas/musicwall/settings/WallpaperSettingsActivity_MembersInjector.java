package com.jfcardenas.musicwall.settings;

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
public final class WallpaperSettingsActivity_MembersInjector implements MembersInjector<WallpaperSettingsActivity> {
  private final Provider<GetMusicImagesUseCase> getMusicImagesProvider;

  private final Provider<ImageLoader> imageLoaderProvider;

  private final Provider<WallpaperRendererFactory> rendererFactoryProvider;

  private WallpaperSettingsActivity_MembersInjector(
      Provider<GetMusicImagesUseCase> getMusicImagesProvider,
      Provider<ImageLoader> imageLoaderProvider,
      Provider<WallpaperRendererFactory> rendererFactoryProvider) {
    this.getMusicImagesProvider = getMusicImagesProvider;
    this.imageLoaderProvider = imageLoaderProvider;
    this.rendererFactoryProvider = rendererFactoryProvider;
  }

  @Override
  public void injectMembers(WallpaperSettingsActivity instance) {
    injectGetMusicImages(instance, getMusicImagesProvider.get());
    injectImageLoader(instance, imageLoaderProvider.get());
    injectRendererFactory(instance, rendererFactoryProvider.get());
  }

  public static MembersInjector<WallpaperSettingsActivity> create(
      Provider<GetMusicImagesUseCase> getMusicImagesProvider,
      Provider<ImageLoader> imageLoaderProvider,
      Provider<WallpaperRendererFactory> rendererFactoryProvider) {
    return new WallpaperSettingsActivity_MembersInjector(getMusicImagesProvider, imageLoaderProvider, rendererFactoryProvider);
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.settings.WallpaperSettingsActivity.getMusicImages")
  public static void injectGetMusicImages(WallpaperSettingsActivity instance,
      GetMusicImagesUseCase getMusicImages) {
    instance.getMusicImages = getMusicImages;
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.settings.WallpaperSettingsActivity.imageLoader")
  public static void injectImageLoader(WallpaperSettingsActivity instance,
      ImageLoader imageLoader) {
    instance.imageLoader = imageLoader;
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.settings.WallpaperSettingsActivity.rendererFactory")
  public static void injectRendererFactory(WallpaperSettingsActivity instance,
      WallpaperRendererFactory rendererFactory) {
    instance.rendererFactory = rendererFactory;
  }
}
