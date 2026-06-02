package com.jfcardenas.musicwall.scrobble;

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
public final class NowPlayingBus_Factory implements Factory<NowPlayingBus> {
  @Override
  public NowPlayingBus get() {
    return newInstance();
  }

  public static NowPlayingBus_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NowPlayingBus newInstance() {
    return new NowPlayingBus();
  }

  private static final class InstanceHolder {
    static final NowPlayingBus_Factory INSTANCE = new NowPlayingBus_Factory();
  }
}
