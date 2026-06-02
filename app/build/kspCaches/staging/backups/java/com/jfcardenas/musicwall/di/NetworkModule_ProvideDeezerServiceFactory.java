package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.DeezerService;
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
public final class NetworkModule_ProvideDeezerServiceFactory implements Factory<DeezerService> {
  private final Provider<OkHttpClient> httpClientProvider;

  private NetworkModule_ProvideDeezerServiceFactory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public DeezerService get() {
    return provideDeezerService(httpClientProvider.get());
  }

  public static NetworkModule_ProvideDeezerServiceFactory create(
      Provider<OkHttpClient> httpClientProvider) {
    return new NetworkModule_ProvideDeezerServiceFactory(httpClientProvider);
  }

  public static DeezerService provideDeezerService(OkHttpClient httpClient) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideDeezerService(httpClient));
  }
}
