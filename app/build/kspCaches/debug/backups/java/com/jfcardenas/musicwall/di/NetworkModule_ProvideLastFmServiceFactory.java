package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.LastFmService;
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
public final class NetworkModule_ProvideLastFmServiceFactory implements Factory<LastFmService> {
  private final Provider<OkHttpClient> httpClientProvider;

  private NetworkModule_ProvideLastFmServiceFactory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public LastFmService get() {
    return provideLastFmService(httpClientProvider.get());
  }

  public static NetworkModule_ProvideLastFmServiceFactory create(
      Provider<OkHttpClient> httpClientProvider) {
    return new NetworkModule_ProvideLastFmServiceFactory(httpClientProvider);
  }

  public static LastFmService provideLastFmService(OkHttpClient httpClient) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideLastFmService(httpClient));
  }
}
