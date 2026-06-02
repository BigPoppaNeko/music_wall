package com.jfcardenas.musicwall.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ArtistImageResolver_Factory implements Factory<ArtistImageResolver> {
  private final Provider<OkHttpClient> httpClientProvider;

  private ArtistImageResolver_Factory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public ArtistImageResolver get() {
    return newInstance(httpClientProvider.get());
  }

  public static ArtistImageResolver_Factory create(Provider<OkHttpClient> httpClientProvider) {
    return new ArtistImageResolver_Factory(httpClientProvider);
  }

  public static ArtistImageResolver newInstance(OkHttpClient httpClient) {
    return new ArtistImageResolver(httpClient);
  }
}
