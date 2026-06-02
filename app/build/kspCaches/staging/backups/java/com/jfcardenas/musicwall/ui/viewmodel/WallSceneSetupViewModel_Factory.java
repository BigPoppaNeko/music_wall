package com.jfcardenas.musicwall.ui.viewmodel;

import android.content.Context;
import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao;
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
public final class WallSceneSetupViewModel_Factory implements Factory<WallSceneSetupViewModel> {
  private final Provider<FavoriteAlbumDao> favoriteDaoProvider;

  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<Context> contextProvider;

  private WallSceneSetupViewModel_Factory(Provider<FavoriteAlbumDao> favoriteDaoProvider,
      Provider<LastFmService> lastFmServiceProvider, Provider<Context> contextProvider) {
    this.favoriteDaoProvider = favoriteDaoProvider;
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public WallSceneSetupViewModel get() {
    return newInstance(favoriteDaoProvider.get(), lastFmServiceProvider.get(), contextProvider.get());
  }

  public static WallSceneSetupViewModel_Factory create(
      Provider<FavoriteAlbumDao> favoriteDaoProvider, Provider<LastFmService> lastFmServiceProvider,
      Provider<Context> contextProvider) {
    return new WallSceneSetupViewModel_Factory(favoriteDaoProvider, lastFmServiceProvider, contextProvider);
  }

  public static WallSceneSetupViewModel newInstance(FavoriteAlbumDao favoriteDao,
      LastFmService lastFmService, Context context) {
    return new WallSceneSetupViewModel(favoriteDao, lastFmService, context);
  }
}
