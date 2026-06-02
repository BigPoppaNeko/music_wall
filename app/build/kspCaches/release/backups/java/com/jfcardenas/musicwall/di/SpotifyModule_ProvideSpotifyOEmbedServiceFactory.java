package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.SpotifyOEmbedService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SpotifyModule_ProvideSpotifyOEmbedServiceFactory implements Factory<SpotifyOEmbedService> {
  @Override
  public SpotifyOEmbedService get() {
    return provideSpotifyOEmbedService();
  }

  public static SpotifyModule_ProvideSpotifyOEmbedServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SpotifyOEmbedService provideSpotifyOEmbedService() {
    return Preconditions.checkNotNullFromProvides(SpotifyModule.INSTANCE.provideSpotifyOEmbedService());
  }

  private static final class InstanceHolder {
    static final SpotifyModule_ProvideSpotifyOEmbedServiceFactory INSTANCE = new SpotifyModule_ProvideSpotifyOEmbedServiceFactory();
  }
}
