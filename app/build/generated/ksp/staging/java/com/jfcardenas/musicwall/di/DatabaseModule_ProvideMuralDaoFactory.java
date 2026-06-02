package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.data.local.db.MusicWallDatabase;
import com.jfcardenas.musicwall.data.local.db.dao.MuralDao;
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
public final class DatabaseModule_ProvideMuralDaoFactory implements Factory<MuralDao> {
  private final Provider<MusicWallDatabase> dbProvider;

  private DatabaseModule_ProvideMuralDaoFactory(Provider<MusicWallDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MuralDao get() {
    return provideMuralDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideMuralDaoFactory create(
      Provider<MusicWallDatabase> dbProvider) {
    return new DatabaseModule_ProvideMuralDaoFactory(dbProvider);
  }

  public static MuralDao provideMuralDao(MusicWallDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMuralDao(db));
  }
}
