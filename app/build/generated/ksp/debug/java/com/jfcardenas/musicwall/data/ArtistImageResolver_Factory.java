package com.jfcardenas.musicwall.data;

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
public final class ArtistImageResolver_Factory implements Factory<ArtistImageResolver> {
  @Override
  public ArtistImageResolver get() {
    return newInstance();
  }

  public static ArtistImageResolver_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ArtistImageResolver newInstance() {
    return new ArtistImageResolver();
  }

  private static final class InstanceHolder {
    static final ArtistImageResolver_Factory INSTANCE = new ArtistImageResolver_Factory();
  }
}
