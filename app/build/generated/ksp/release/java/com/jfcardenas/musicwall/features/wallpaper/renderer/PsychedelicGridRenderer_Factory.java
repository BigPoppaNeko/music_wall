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
public final class PsychedelicGridRenderer_Factory implements Factory<PsychedelicGridRenderer> {
  @Override
  public PsychedelicGridRenderer get() {
    return newInstance();
  }

  public static PsychedelicGridRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PsychedelicGridRenderer newInstance() {
    return new PsychedelicGridRenderer();
  }

  private static final class InstanceHolder {
    static final PsychedelicGridRenderer_Factory INSTANCE = new PsychedelicGridRenderer_Factory();
  }
}
