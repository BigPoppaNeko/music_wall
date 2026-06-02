package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.LyricsService;
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
public final class NetworkModule_ProvideLyricsServiceFactory implements Factory<LyricsService> {
  private final Provider<OkHttpClient> httpClientProvider;

  private NetworkModule_ProvideLyricsServiceFactory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public LyricsService get() {
    return provideLyricsService(httpClientProvider.get());
  }

  public static NetworkModule_ProvideLyricsServiceFactory create(
      Provider<OkHttpClient> httpClientProvider) {
    return new NetworkModule_ProvideLyricsServiceFactory(httpClientProvider);
  }

  public static LyricsService provideLyricsService(OkHttpClient httpClient) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideLyricsService(httpClient));
  }
}
