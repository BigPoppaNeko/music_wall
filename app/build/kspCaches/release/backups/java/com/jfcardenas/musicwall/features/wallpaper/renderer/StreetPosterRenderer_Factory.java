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
public final class StreetPosterRenderer_Factory implements Factory<StreetPosterRenderer> {
  @Override
  public StreetPosterRenderer get() {
    return newInstance();
  }

  public static StreetPosterRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static StreetPosterRenderer newInstance() {
    return new StreetPosterRenderer();
  }

  private static final class InstanceHolder {
    static final StreetPosterRenderer_Factory INSTANCE = new StreetPosterRenderer_Factory();
  }
}
