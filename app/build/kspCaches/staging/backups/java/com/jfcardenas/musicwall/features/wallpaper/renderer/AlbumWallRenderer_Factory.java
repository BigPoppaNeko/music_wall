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
public final class AlbumWallRenderer_Factory implements Factory<AlbumWallRenderer> {
  @Override
  public AlbumWallRenderer get() {
    return newInstance();
  }

  public static AlbumWallRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AlbumWallRenderer newInstance() {
    return new AlbumWallRenderer();
  }

  private static final class InstanceHolder {
    static final AlbumWallRenderer_Factory INSTANCE = new AlbumWallRenderer_Factory();
  }
}
