package com.jfcardenas.musicwall.ui.viewmodel;

import android.content.Context;
import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.data.CoverFallbackRepository;
import com.jfcardenas.musicwall.data.CoverUpdateBus;
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao;
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

  private final Provider<CoverFallbackRepository> coverFallbackRepoProvider;

  private final Provider<CoverUpdateBus> coverBusProvider;

  private final Provider<Context> contextProvider;

  private HomeViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<AlbumDao> albumDaoProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<CoverUpdateBus> coverBusProvider, Provider<Context> contextProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.albumDaoProvider = albumDaoProvider;
    this.coverFallbackRepoProvider = coverFallbackRepoProvider;
    this.coverBusProvider = coverBusProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(lastFmServiceProvider.get(), albumDaoProvider.get(), coverFallbackRepoProvider.get(), coverBusProvider.get(), contextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<AlbumDao> albumDaoProvider,
      Provider<CoverFallbackRepository> coverFallbackRepoProvider,
      Provider<CoverUpdateBus> coverBusProvider, Provider<Context> contextProvider) {
    return new HomeViewModel_Factory(lastFmServiceProvider, albumDaoProvider, coverFallbackRepoProvider, coverBusProvider, contextProvider);
  }

  public static HomeViewModel newInstance(LastFmService lastFmService, AlbumDao albumDao,
      CoverFallbackRepository coverFallbackRepo, CoverUpdateBus coverBus, Context context) {
    return new HomeViewModel(lastFmService, albumDao, coverFallbackRepo, coverBus, context);
  }
}
