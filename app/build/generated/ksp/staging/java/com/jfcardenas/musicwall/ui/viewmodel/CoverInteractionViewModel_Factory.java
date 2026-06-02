package com.jfcardenas.musicwall.ui.viewmodel;

import com.jfcardenas.musicwall.api.LastFmService;
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
public final class CoverInteractionViewModel_Factory implements Factory<CoverInteractionViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<FavoriteAlbumDao> favoriteDaoProvider;

  private CoverInteractionViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<FavoriteAlbumDao> favoriteDaoProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
  }

  @Override
  public CoverInteractionViewModel get() {
    return newInstance(lastFmServiceProvider.get(), favoriteDaoProvider.get());
  }

  public static CoverInteractionViewModel_Factory create(
      Provider<LastFmService> lastFmServiceProvider,
      Provider<FavoriteAlbumDao> favoriteDaoProvider) {
    return new CoverInteractionViewModel_Factory(lastFmServiceProvider, favoriteDaoProvider);
  }

  public static CoverInteractionViewModel newInstance(LastFmService lastFmService,
      FavoriteAlbumDao favoriteDao) {
    return new CoverInteractionViewModel(lastFmService, favoriteDao);
  }
}
