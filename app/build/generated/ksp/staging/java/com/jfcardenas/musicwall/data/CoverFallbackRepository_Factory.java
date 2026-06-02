package com.jfcardenas.musicwall.data;

import android.content.Context;
import com.jfcardenas.musicwall.api.DeezerService;
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CoverFallbackRepository_Factory implements Factory<CoverFallbackRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<DeezerService> deezerServiceProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  private final Provider<CoverUpdateBus> coverBusProvider;

  private CoverFallbackRepository_Factory(Provider<Context> contextProvider,
      Provider<DeezerService> deezerServiceProvider, Provider<AlbumDao> albumDaoProvider,
      Provider<CoverUpdateBus> coverBusProvider) {
    this.contextProvider = contextProvider;
    this.deezerServiceProvider = deezerServiceProvider;
    this.albumDaoProvider = albumDaoProvider;
    this.coverBusProvider = coverBusProvider;
  }

  @Override
  public CoverFallbackRepository get() {
    return newInstance(contextProvider.get(), deezerServiceProvider.get(), albumDaoProvider.get(), coverBusProvider.get());
  }

  public static CoverFallbackRepository_Factory create(Provider<Context> contextProvider,
      Provider<DeezerService> deezerServiceProvider, Provider<AlbumDao> albumDaoProvider,
      Provider<CoverUpdateBus> coverBusProvider) {
    return new CoverFallbackRepository_Factory(contextProvider, deezerServiceProvider, albumDaoProvider, coverBusProvider);
  }

  public static CoverFallbackRepository newInstance(Context context, DeezerService deezerService,
      AlbumDao albumDao, CoverUpdateBus coverBus) {
    return new CoverFallbackRepository(context, deezerService, albumDao, coverBus);
  }
}
