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
public final class PhysicalCollageRenderer_Factory implements Factory<PhysicalCollageRenderer> {
  @Override
  public PhysicalCollageRenderer get() {
    return newInstance();
  }

  public static PhysicalCollageRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PhysicalCollageRenderer newInstance() {
    return new PhysicalCollageRenderer();
  }

  private static final class InstanceHolder {
    static final PhysicalCollageRenderer_Factory INSTANCE = new PhysicalCollageRenderer_Factory();
  }
}
