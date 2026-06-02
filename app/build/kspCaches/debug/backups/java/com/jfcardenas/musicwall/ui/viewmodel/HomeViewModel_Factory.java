package com.jfcardenas.musicwall.ui.viewmodel;

import android.content.Context;
import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.auth.UserSessionRepository;
import com.jfcardenas.musicwall.data.CoverFallbackRepository;
import com.jfcardenas.musicwall.data.CoverUpdateBus;
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao;
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao;
import com.jfcardenas.musicwall.data.local.db.dao.VetoedAlbumDao;
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase;
import com.jfcardenas.musicwall.scrobble.NowPlayingBus;
import com.jfcardenas.musicwall.scrobble.ScrobbleRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  private final Provider<FavoriteAlbumDao> favoriteDaoProvider;

  private final Provider<VetoedAlbumDao> vetoedAlbumDaoProvider;

  private final Provider<CoverFallbackRepository> coverFallbackRepoProvider;

  private final Provider<CoverUpdateBus> coverBusProvider;

  private final Provider<UserSessionRepository> userSessionProvider;

  private final Provider<ScrobbleRepository> scrobbleRepositoryProvider;

  private final Provider<NowPlayingBus> nowPlayingBusProvider;

  private final Provider<GetMusicImagesUseCase> getMusicImagesProvider;

  private final Provider<Context> contextProvider;

  private HomeViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<AlbumDao> albumDaoProvider, Provider<FavoriteAlbumDao> favoriteDaoProvider,
      Provider<VetoedAlbumDao> vetoedAlbumDaoProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<CoverUpdateBus> coverBusProvider,
      Provider<UserSessionRepository> userSessionProvider,
      Provider<ScrobbleRepository> scrobbleRepositoryProvider,
      Provider<NowPlayingBus> nowPlayingBusProvider,
      Provider<GetMusicImagesUseCase> getMusicImagesProvider, Provider<Context> contextProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.albumDaoProvider = albumDaoProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
    this.vetoedAlbumDaoProvider = vetoedAlbumDaoProvider;
    this.coverFallbackRepoProvider = coverFallbackRepoProvider;
    this.coverBusProvider = coverBusProvider;
    this.userSessionProvider = userSessionProvider;
    this.scrobbleRepositoryProvider = scrobbleRepositoryProvider;
    this.nowPlayingBusProvider = nowPlayingBusProvider;
    this.getMusicImagesProvider = getMusicImagesProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(lastFmServiceProvider.get(), albumDaoProvider.get(), favoriteDaoProvider.get(), vetoedAlbumDaoProvider.get(), coverFallbackRepoProvider.get(), coverBusProvider.get(), userSessionProvider.get(), scrobbleRepositoryProvider.get(), nowPlayingBusProvider.get(), getMusicImagesProvider.get(), contextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<AlbumDao> albumDaoProvider, Provider<FavoriteAlbumDao> favoriteDaoProvider,
      Provider<VetoedAlbumDao> vetoedAlbumDaoProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<CoverUpdateBus> coverBusProvider,
      Provider<UserSessionRepository> userSessionProvider,
      Provider<ScrobbleRepository> scrobbleRepositoryProvider,
      Provider<NowPlayingBus> nowPlayingBusProvider,
      Provider<GetMusicImagesUseCase> getMusicImagesProvider, Provider<Context> contextProvider) {
    return new HomeViewModel_Factory(lastFmServiceProvider, albumDaoProvider, favoriteDaoProvider, vetoedAlbumDaoProvider, coverFallbackRepoProvider, coverBusProvider, userSessionProvider, scrobbleRepositoryProvider, nowPlayingBusProvider, getMusicImagesProvider, contextProvider);
  }

  public static HomeViewModel newInstance(LastFmService lastFmService, AlbumDao albumDao,
      FavoriteAlbumDao favoriteDao, VetoedAlbumDao vetoedAlbumDao,
      CoverFallbackRepository coverFallbackRepo, CoverUpdateBus coverBus,
      UserSessionRepository userSession, ScrobbleRepository scrobbleRepository,
      NowPlayingBus nowPlayingBus, GetMusicImagesUseCase getMusicImages, Context context) {
    return new HomeViewModel(lastFmService, albumDao, favoriteDao, vetoedAlbumDao, coverFallbackRepo, coverBus, userSession, scrobbleRepository, nowPlayingBus, getMusicImages, context);
  }
}
