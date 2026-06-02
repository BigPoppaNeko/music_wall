package com.jfcardenas.musicwall.features.wallpaper.renderer;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class CinematicWallRenderer_Factory implements Factory<CinematicWallRenderer> {
  @Override
  public CinematicWallRenderer get() {
    return newInstance();
  }

  public static CinematicWallRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CinematicWallRenderer newInstance() {
    return new CinematicWallRenderer();
  }

  private static final class InstanceHolder {
    static final CinematicWallRenderer_Factory INSTANCE = new CinematicWallRenderer_Factory();
  }
}
