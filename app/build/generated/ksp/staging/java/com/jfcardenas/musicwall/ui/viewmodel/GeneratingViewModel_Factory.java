package com.jfcardenas.musicwall.ui.viewmodel;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import coil.ImageLoader;
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase;
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRendererFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class GeneratingViewModel_Factory implements Factory<GeneratingViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<GetMusicImagesUseCase> getMusicImagesProvider;

  private final Provider<WallpaperRendererFactory> rendererFactoryProvider;

  private final Provider<ImageLoader> imageLoaderProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private GeneratingViewModel_Factory(Provider<Context> contextProvider,
      Provider<GetMusicImagesUseCase> getMusicImagesProvider,
      Provider<WallpaperRendererFactory> rendererFactoryProvider,
      Provider<ImageLoader> imageLoaderProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.contextProvider = contextProvider;
    this.getMusicImagesProvider = getMusicImagesProvider;
    this.rendererFactoryProvider = rendererFactoryProvider;
    this.imageLoaderProvider = imageLoaderProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public GeneratingViewModel get() {
    return newInstance(contextProvider.get(), getMusicImagesProvider.get(), rendererFactoryProvider.get(), imageLoaderProvider.get(), savedStateHandleProvider.get());
  }

  public static GeneratingViewModel_Factory create(Provider<Context> contextProvider,
      Provider<GetMusicImagesUseCase> getMusicImagesProvider,
      Provider<WallpaperRendererFactory> rendererFactoryProvider,
      Provider<ImageLoader> imageLoaderProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new GeneratingViewModel_Factory(contextProvider, getMusicImagesProvider, rendererFactoryProvider, imageLoaderProvider, savedStateHandleProvider);
  }

  public static GeneratingViewModel newInstance(Context context,
      GetMusicImagesUseCase getMusicImages, WallpaperRendererFactory rendererFactory,
      ImageLoader imageLoader, SavedStateHandle savedStateHandle) {
    return new GeneratingViewModel(context, getMusicImages, rendererFactory, imageLoader, savedStateHandle);
  }
}
