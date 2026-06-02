package com.jfcardenas.musicwall.ui.viewmodel;

import android.content.Context;
import com.jfcardenas.musicwall.api.DeezerService;
import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.data.CoverFallbackRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ArtistasViewModel_Factory implements Factory<ArtistasViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<DeezerService> deezerServiceProvider;

  private final Provider<CoverFallbackRepository> coverFallbackRepoProvider;

  private final Provider<Context> contextProvider;

  private ArtistasViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<DeezerService> deezerServiceProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<Context> contextProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.deezerServiceProvider = deezerServiceProvider;
    this.coverFallbackRepoProvider = coverFallbackRepoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ArtistasViewModel get() {
    return newInstance(lastFmServiceProvider.get(), deezerServiceProvider.get(), coverFallbackRepoProvider.get(), contextProvider.get());
  }

  public static ArtistasViewModel_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<DeezerService> deezerServiceProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<Context> contextProvider) {
    return new ArtistasViewModel_Factory(lastFmServiceProvider, deezerServiceProvider, coverFallbackRepoProvider, contextProvider);
  }

  public static ArtistasViewModel newInstance(LastFmService lastFmService,
      DeezerService deezerService, CoverFallbackRepository coverFallbackRepo, Context context) {
    return new ArtistasViewModel(lastFmService, deezerService, coverFallbackRepo, context);
  }
}
