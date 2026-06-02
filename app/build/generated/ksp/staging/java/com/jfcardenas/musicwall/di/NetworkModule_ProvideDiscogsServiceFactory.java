package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.DiscogsService;
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
public final class NetworkModule_ProvideDiscogsServiceFactory implements Factory<DiscogsService> {
  private final Provider<OkHttpClient> httpClientProvider;

  private NetworkModule_ProvideDiscogsServiceFactory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public DiscogsService get() {
    return provideDiscogsService(httpClientProvider.get());
  }

  public static NetworkModule_ProvideDiscogsServiceFactory create(
      Provider<OkHttpClient> httpClientProvider) {
    return new NetworkModule_ProvideDiscogsServiceFactory(httpClientProvider);
  }

  public static DiscogsService provideDiscogsService(OkHttpClient httpClient) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideDiscogsService(httpClient));
  }
}
