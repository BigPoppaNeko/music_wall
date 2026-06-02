package com.jfcardenas.musicwall.ui.viewmodel;

import com.jfcardenas.musicwall.api.SpotifyOEmbedService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class SpotifySourceViewModel_Factory implements Factory<SpotifySourceViewModel> {
  private final Provider<SpotifyOEmbedService> spotifyServiceProvider;

  private SpotifySourceViewModel_Factory(Provider<SpotifyOEmbedService> spotifyServiceProvider) {
    this.spotifyServiceProvider = spotifyServiceProvider;
  }

  @Override
  public SpotifySourceViewModel get() {
    return newInstance(spotifyServiceProvider.get());
  }

  public static SpotifySourceViewModel_Factory create(
      Provider<SpotifyOEmbedService> spotifyServiceProvider) {
    return new SpotifySourceViewModel_Factory(spotifyServiceProvider);
  }

  public static SpotifySourceViewModel newInstance(SpotifyOEmbedService spotifyService) {
    return new SpotifySourceViewModel(spotifyService);
  }
}
