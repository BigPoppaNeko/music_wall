package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.data.local.db.MusicWallDatabase;
import com.jfcardenas.musicwall.data.local.db.dao.ArtistDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideArtistDaoFactory implements Factory<ArtistDao> {
  private final Provider<MusicWallDatabase> dbProvider;

  private DatabaseModule_ProvideArtistDaoFactory(Provider<MusicWallDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ArtistDao get() {
    return provideArtistDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideArtistDaoFactory create(
      Provider<MusicWallDatabase> dbProvider) {
    return new DatabaseModule_ProvideArtistDaoFactory(dbProvider);
  }

  public static ArtistDao provideArtistDao(MusicWallDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideArtistDao(db));
  }
}
