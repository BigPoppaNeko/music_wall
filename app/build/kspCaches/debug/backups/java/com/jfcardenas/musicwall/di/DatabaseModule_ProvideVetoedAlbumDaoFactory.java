package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.data.local.db.MusicWallDatabase;
import com.jfcardenas.musicwall.data.local.db.dao.VetoedAlbumDao;
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
public final class DatabaseModule_ProvideVetoedAlbumDaoFactory implements Factory<VetoedAlbumDao> {
  private final Provider<MusicWallDatabase> dbProvider;

  private DatabaseModule_ProvideVetoedAlbumDaoFactory(Provider<MusicWallDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public VetoedAlbumDao get() {
    return provideVetoedAlbumDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideVetoedAlbumDaoFactory create(
      Provider<MusicWallDatabase> dbProvider) {
    return new DatabaseModule_ProvideVetoedAlbumDaoFactory(dbProvider);
  }

  public static VetoedAlbumDao provideVetoedAlbumDao(MusicWallDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideVetoedAlbumDao(db));
  }
}
