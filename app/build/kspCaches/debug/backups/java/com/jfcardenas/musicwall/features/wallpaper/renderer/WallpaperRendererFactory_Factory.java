package com.jfcardenas.musicwall.features.wallpaper.renderer;

import android.content.Context;
import com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "dagger.hilt.android.qualifiers.ApplicationContext",
    "javax.inject.Named"
})
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

  private final Provider<MosaicBlendRenderer> mosaicRendererProvider;

  private final Provider<PuzzleRenderer> puzzleRendererProvider;

  private final Provider<PsychedelicGridRenderer> psychedelicRendererProvider;

  private final Provider<ManchesterWallRenderer> manchesterRendererProvider;

  private final Provider<OrganicRenderer> britrockSceneProvider;

  private final Provider<OrganicRenderer> banoBarLimaSceneProvider;

  private final Provider<OrganicRenderer> woodstockSceneProvider;

  private WallpaperRendererFactory_Factory(Provider<Context> contextProvider,
      Provider<MosaicBlendRenderer> mosaicRendererProvider,
      Provider<PuzzleRenderer> puzzleRendererProvider,
      Provider<PsychedelicGridRenderer> psychedelicRendererProvider,
      Provider<ManchesterWallRenderer> manchesterRendererProvider,
      Provider<OrganicRenderer> britrockSceneProvider,
      Provider<OrganicRenderer> banoBarLimaSceneProvider,
      Provider<OrganicRenderer> woodstockSceneProvider) {
    this.contextProvider = contextProvider;
    this.mosaicRendererProvider = mosaicRendererProvider;
    this.puzzleRendererProvider = puzzleRendererProvider;
    this.psychedelicRendererProvider = psychedelicRendererProvider;
    this.manchesterRendererProvider = manchesterRendererProvider;
    this.britrockSceneProvider = britrockSceneProvider;
    this.banoBarLimaSceneProvider = banoBarLimaSceneProvider;
    this.woodstockSceneProvider = woodstockSceneProvider;
  }

  @Override
  public WallpaperRendererFactory get() {
    return newInstance(contextProvider.get(), mosaicRendererProvider.get(), puzzleRendererProvider.get(), psychedelicRendererProvider.get(), manchesterRendererProvider.get(), britrockSceneProvider.get(), banoBarLimaSceneProvider.get(), woodstockSceneProvider.get());
  }

  public static WallpaperRendererFactory_Factory create(Provider<Context> contextProvider,
      Provider<MosaicBlendRenderer> mosaicRendererProvider,
      Provider<PuzzleRenderer> puzzleRendererProvider,
      Provider<PsychedelicGridRenderer> psychedelicRendererProvider,
      Provider<ManchesterWallRenderer> manchesterRendererProvider,
      Provider<OrganicRenderer> britrockSceneProvider,
      Provider<OrganicRenderer> banoBarLimaSceneProvider,
      Provider<OrganicRenderer> woodstockSceneProvider) {
    return new WallpaperRendererFactory_Factory(contextProvider, mosaicRendererProvider, puzzleRendererProvider, psychedelicRendererProvider, manchesterRendererProvider, britrockSceneProvider, banoBarLimaSceneProvider, woodstockSceneProvider);
  }

  public static WallpaperRendererFactory newInstance(Context context,
      MosaicBlendRenderer mosaicRenderer, PuzzleRenderer puzzleRenderer,
      PsychedelicGridRenderer psychedelicRenderer, ManchesterWallRenderer manchesterRenderer,
      OrganicRenderer britrockScene, OrganicRenderer banoBarLimaScene,
      OrganicRenderer woodstockScene) {
    return new WallpaperRendererFactory(context, mosaicRenderer, puzzleRenderer, psychedelicRenderer, manchesterRenderer, britrockScene, banoBarLimaScene, woodstockScene);
  }
}
