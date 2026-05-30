package com.jfcardenas.musicwall.features.wallpaper.renderer;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class WallpaperRendererFactory_Factory implements Factory<WallpaperRendererFactory> {
  private final Provider<Context> contextProvider;

  private final Provider<StreetPosterRenderer> streetRendererProvider;

  private final Provider<EcosystemRenderer> ecosystemRendererProvider;

  private final Provider<AlbumWallRenderer> albumRendererProvider;

  private final Provider<CinematicWallRenderer> cinematicRendererProvider;

  private final Provider<PhysicalCollageRenderer> physicalRendererProvider;

  private WallpaperRendererFactory_Factory(Provider<Context> contextProvider,
      Provider<StreetPosterRenderer> streetRendererProvider,
      Provider<EcosystemRenderer> ecosystemRendererProvider,
      Provider<AlbumWallRenderer> albumRendererProvider,
      Provider<CinematicWallRenderer> cinematicRendererProvider,
      Provider<PhysicalCollageRenderer> physicalRendererProvider) {
    this.contextProvider = contextProvider;
    this.streetRendererProvider = streetRendererProvider;
    this.ecosystemRendererProvider = ecosystemRendererProvider;
    this.albumRendererProvider = albumRendererProvider;
    this.cinematicRendererProvider = cinematicRendererProvider;
    this.physicalRendererProvider = physicalRendererProvider;
  }

  @Override
  public WallpaperRendererFactory get() {
    return newInstance(contextProvider.get(), streetRendererProvider.get(), ecosystemRendererProvider.get(), albumRendererProvider.get(), cinematicRendererProvider.get(), physicalRendererProvider.get());
  }

  public static WallpaperRendererFactory_Factory create(Provider<Context> contextProvider,
      Provider<StreetPosterRenderer> streetRendererProvider,
      Provider<EcosystemRenderer> ecosystemRendererProvider,
      Provider<AlbumWallRenderer> albumRendererProvider,
      Provider<CinematicWallRenderer> cinematicRendererProvider,
      Provider<PhysicalCollageRenderer> physicalRendererProvider) {
    return new WallpaperRendererFactory_Factory(contextProvider, streetRendererProvider, ecosystemRendererProvider, albumRendererProvider, cinematicRendererProvider, physicalRendererProvider);
  }

  public static WallpaperRendererFactory newInstance(Context context,
      StreetPosterRenderer streetRenderer, EcosystemRenderer ecosystemRenderer,
      AlbumWallRenderer albumRenderer, CinematicWallRenderer cinematicRenderer,
      PhysicalCollageRenderer physicalRenderer) {
    return new WallpaperRendererFactory(context, streetRenderer, ecosystemRenderer, albumRenderer, cinematicRenderer, physicalRenderer);
  }
}
