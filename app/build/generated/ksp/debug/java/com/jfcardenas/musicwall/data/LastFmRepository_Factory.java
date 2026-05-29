package com.jfcardenas.musicwall.data;

import android.content.Context;
import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao;
import com.jfcardenas.musicwall.data.local.db.dao.ArtistDao;
import com.jfcardenas.musicwall.data.local.db.dao.TrackDao;
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
public final class LastFmRepository_Factory implements Factory<LastFmRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<LastFmService> serviceProvider;

  private final Provider<AlbumDao> albumDaoProvider;

  private final Provider<ArtistDao> artistDaoProvider;

  private final Provider<TrackDao> trackDaoProvider;

  private LastFmRepository_Factory(Provider<Context> contextProvider,
      Provider<LastFmService> serviceProvider, Provider<AlbumDao> albumDaoProvider,
      Provider<ArtistDao> artistDaoProvider, Provider<TrackDao> trackDaoProvider) {
    this.contextProvider = contextProvider;
    this.serviceProvider = serviceProvider;
    this.albumDaoProvider = albumDaoProvider;
    this.artistDaoProvider = artistDaoProvider;
    this.trackDaoProvider = trackDaoProvider;
  }

  @Override
  public LastFmRepository get() {
    return newInstance(contextProvider.get(), serviceProvider.get(), albumDaoProvider.get(), artistDaoProvider.get(), trackDaoProvider.get());
  }

  public static LastFmRepository_Factory create(Provider<Context> contextProvider,
      Provider<LastFmService> serviceProvider, Provider<AlbumDao> albumDaoProvider,
      Provider<ArtistDao> artistDaoProvider, Provider<TrackDao> trackDaoProvider) {
    return new LastFmRepository_Factory(contextProvider, serviceProvider, albumDaoProvider, artistDaoProvider, trackDaoProvider);
  }

  public static LastFmRepository newInstance(Context context, LastFmService service,
      AlbumDao albumDao, ArtistDao artistDao, TrackDao trackDao) {
    return new LastFmRepository(context, service, albumDao, artistDao, trackDao);
  }
}
