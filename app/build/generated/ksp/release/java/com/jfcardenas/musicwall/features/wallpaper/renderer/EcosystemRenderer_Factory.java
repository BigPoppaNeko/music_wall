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
public final class EcosystemRenderer_Factory implements Factory<EcosystemRenderer> {
  @Override
  public EcosystemRenderer get() {
    return newInstance();
  }

  public static EcosystemRenderer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EcosystemRenderer newInstance() {
    return new EcosystemRenderer();
  }

  private static final class InstanceHolder {
    static final EcosystemRenderer_Factory INSTANCE = new EcosystemRenderer_Factory();
  }
}
