package com.jfcardenas.musicwall.ui.viewmodel;

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
public final class FavoritesViewModel_Factory implements Factory<FavoritesViewModel> {
  private final Provider<FavoriteAlbumDao> daoProvider;

  private FavoritesViewModel_Factory(Provider<FavoriteAlbumDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public FavoritesViewModel get() {
    return newInstance(daoProvider.get());
  }

  public static FavoritesViewModel_Factory create(Provider<FavoriteAlbumDao> daoProvider) {
    return new FavoritesViewModel_Factory(daoProvider);
  }

  public static FavoritesViewModel newInstance(FavoriteAlbumDao dao) {
    return new FavoritesViewModel(dao);
  }
}
