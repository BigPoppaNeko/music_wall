package com.jfcardenas.musicwall.features.wallpaper.renderer;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ManchesterWallRenderer_Factory implements Factory<ManchesterWallRenderer> {
  @Override
  public ManchesterWallRenderer get() {
    return newInstance();
  }

  public static ManchesterWallRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ManchesterWallRenderer newInstance() {
    return new ManchesterWallRenderer();
  }

  private static final class InstanceHolder {
    static final ManchesterWallRenderer_Factory INSTANCE = new ManchesterWallRenderer_Factory();
  }
}
