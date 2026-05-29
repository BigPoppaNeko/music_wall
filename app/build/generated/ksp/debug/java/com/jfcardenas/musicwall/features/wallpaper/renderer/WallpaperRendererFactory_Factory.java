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
  private final Provider<EcosystemRenderer> ecosystemRendererProvider;

  private WallpaperRendererFactory_Factory(Provider<EcosystemRenderer> ecosystemRendererProvider) {
    this.ecosystemRendererProvider = ecosystemRendererProvider;
  }

  @Override
  public WallpaperRendererFactory get() {
    return newInstance(ecosystemRendererProvider.get());
  }

  public static WallpaperRendererFactory_Factory create(
      Provider<EcosystemRenderer> ecosystemRendererProvider) {
    return new WallpaperRendererFactory_Factory(ecosystemRendererProvider);
  }

  public static WallpaperRendererFactory newInstance(EcosystemRenderer ecosystemRenderer) {
    return new WallpaperRendererFactory(ecosystemRenderer);
  }
}
