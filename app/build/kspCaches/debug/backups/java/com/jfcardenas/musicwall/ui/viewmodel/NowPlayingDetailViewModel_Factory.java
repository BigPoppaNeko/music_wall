package com.jfcardenas.musicwall.ui.viewmodel;

import com.jfcardenas.musicwall.api.DiscogsService;
import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.api.LyricsService;
import com.jfcardenas.musicwall.data.CoverFallbackRepository;
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao;
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
public final class NowPlayingDetailViewModel_Factory implements Factory<NowPlayingDetailViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<LyricsService> lyricsServiceProvider;

  private final Provider<DiscogsService> discogsServiceProvider;

  private final Provider<CoverFallbackRepository> coverFallbackRepoProvider;

  private final Provider<FavoriteAlbumDao> favoriteDaoProvider;

  private NowPlayingDetailViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<LyricsService> lyricsServiceProvider,
      Provider<DiscogsService> discogsServiceProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<FavoriteAlbumDao> favoriteDaoProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.lyricsServiceProvider = lyricsServiceProvider;
    this.discogsServiceProvider = discogsServiceProvider;
    this.coverFallbackRepoProvider = coverFallbackRepoProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
  }

  @Override
  public NowPlayingDetailViewModel get() {
    return newInstance(lastFmServiceProvider.get(), lyricsServiceProvider.get(), discogsServiceProvider.get(), coverFallbackRepoProvider.get(), favoriteDaoProvider.get());
  }

  public static NowPlayingDetailViewModel_Factory create(
      Provider<LastFmService> lastFmServiceProvider, Provider<LyricsService> lyricsServiceProvider,
      Provider<DiscogsService> discogsServiceProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<FavoriteAlbumDao> favoriteDaoProvider) {
    return new NowPlayingDetailViewModel_Factory(lastFmServiceProvider, lyricsServiceProvider, discogsServiceProvider, coverFallbackRepoProvider, favoriteDaoProvider);
  }

  public static NowPlayingDetailViewModel newInstance(LastFmService lastFmService,
      LyricsService lyricsService, DiscogsService discogsService,
      CoverFallbackRepository coverFallbackRepo, FavoriteAlbumDao favoriteDao) {
    return new NowPlayingDetailViewModel(lastFmService, lyricsService, discogsService, coverFallbackRepo, favoriteDao);
  }
}
