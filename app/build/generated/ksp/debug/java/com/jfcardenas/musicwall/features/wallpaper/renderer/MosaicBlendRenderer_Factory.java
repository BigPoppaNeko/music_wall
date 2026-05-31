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
public final class MosaicBlendRenderer_Factory implements Factory<MosaicBlendRenderer> {
  @Override
  public MosaicBlendRenderer get() {
    return newInstance();
  }

  public static MosaicBlendRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MosaicBlendRenderer newInstance() {
    return new MosaicBlendRenderer();
  }

  private static final class InstanceHolder {
    static final MosaicBlendRenderer_Factory INSTANCE = new MosaicBlendRenderer_Factory();
  }
}
