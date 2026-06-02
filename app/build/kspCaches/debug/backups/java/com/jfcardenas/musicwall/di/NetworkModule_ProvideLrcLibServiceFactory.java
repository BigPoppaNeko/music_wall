package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.LrcLibService;
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
public final class NetworkModule_ProvideLrcLibServiceFactory implements Factory<LrcLibService> {
  private final Provider<OkHttpClient> httpClientProvider;

  private NetworkModule_ProvideLrcLibServiceFactory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public LrcLibService get() {
    return provideLrcLibService(httpClientProvider.get());
  }

  public static NetworkModule_ProvideLrcLibServiceFactory create(
      Provider<OkHttpClient> httpClientProvider) {
    return new NetworkModule_ProvideLrcLibServiceFactory(httpClientProvider);
  }

  public static LrcLibService provideLrcLibService(OkHttpClient httpClient) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideLrcLibService(httpClient));
  }
}
