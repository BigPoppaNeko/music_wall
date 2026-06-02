package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.SpotifyOEmbedService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
  private final Provider<OkHttpClient> httpClientProvider;

  private SpotifyModule_ProvideSpotifyOEmbedServiceFactory(
      Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public SpotifyOEmbedService get() {
    return provideSpotifyOEmbedService(httpClientProvider.get());
  }

  public static SpotifyModule_ProvideSpotifyOEmbedServiceFactory create(
      Provider<OkHttpClient> httpClientProvider) {
    return new SpotifyModule_ProvideSpotifyOEmbedServiceFactory(httpClientProvider);
  }

  public static SpotifyOEmbedService provideSpotifyOEmbedService(OkHttpClient httpClient) {
    return Preconditions.checkNotNullFromProvides(SpotifyModule.INSTANCE.provideSpotifyOEmbedService(httpClient));
  }
}
