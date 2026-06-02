package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.data.local.db.MusicWallDatabase;
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao;
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
public final class DatabaseModule_ProvideAlbumDaoFactory implements Factory<AlbumDao> {
  private final Provider<MusicWallDatabase> dbProvider;

  private DatabaseModule_ProvideAlbumDaoFactory(Provider<MusicWallDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AlbumDao get() {
    return provideAlbumDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideAlbumDaoFactory create(
      Provider<MusicWallDatabase> dbProvider) {
    return new DatabaseModule_ProvideAlbumDaoFactory(dbProvider);
  }

  public static AlbumDao provideAlbumDao(MusicWallDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideAlbumDao(db));
  }
}
