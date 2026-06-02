package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.data.local.db.MusicWallDatabase;
import com.jfcardenas.musicwall.data.local.db.dao.LocalScrobbleDao;
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
public final class DatabaseModule_ProvideLocalScrobbleDaoFactory implements Factory<LocalScrobbleDao> {
  private final Provider<MusicWallDatabase> dbProvider;

  private DatabaseModule_ProvideLocalScrobbleDaoFactory(Provider<MusicWallDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LocalScrobbleDao get() {
    return provideLocalScrobbleDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideLocalScrobbleDaoFactory create(
      Provider<MusicWallDatabase> dbProvider) {
    return new DatabaseModule_ProvideLocalScrobbleDaoFactory(dbProvider);
  }

  public static LocalScrobbleDao provideLocalScrobbleDao(MusicWallDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLocalScrobbleDao(db));
  }
}
