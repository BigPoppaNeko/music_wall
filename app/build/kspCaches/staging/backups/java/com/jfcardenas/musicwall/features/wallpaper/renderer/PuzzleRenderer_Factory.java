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
public final class PuzzleRenderer_Factory implements Factory<PuzzleRenderer> {
  @Override
  public PuzzleRenderer get() {
    return newInstance();
  }

  public static PuzzleRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PuzzleRenderer newInstance() {
    return new PuzzleRenderer();
  }

  private static final class InstanceHolder {
    static final PuzzleRenderer_Factory INSTANCE = new PuzzleRenderer_Factory();
  }
}
