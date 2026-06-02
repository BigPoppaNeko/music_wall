package com.jfcardenas.musicwall.scrobble;

import com.jfcardenas.musicwall.data.local.db.dao.LocalScrobbleDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ScrobbleRepository_Factory implements Factory<ScrobbleRepository> {
  private final Provider<LocalScrobbleDao> localScrobbleDaoProvider;

  private final Provider<LastFmTrackResolver> resolverProvider;

  private ScrobbleRepository_Factory(Provider<LocalScrobbleDao> localScrobbleDaoProvider,
      Provider<LastFmTrackResolver> resolverProvider) {
    this.localScrobbleDaoProvider = localScrobbleDaoProvider;
    this.resolverProvider = resolverProvider;
  }

  @Override
  public ScrobbleRepository get() {
    return newInstance(localScrobbleDaoProvider.get(), resolverProvider.get());
  }

  public static ScrobbleRepository_Factory create(
      Provider<LocalScrobbleDao> localScrobbleDaoProvider,
      Provider<LastFmTrackResolver> resolverProvider) {
    return new ScrobbleRepository_Factory(localScrobbleDaoProvider, resolverProvider);
  }

  public static ScrobbleRepository newInstance(LocalScrobbleDao localScrobbleDao,
      LastFmTrackResolver resolver) {
    return new ScrobbleRepository(localScrobbleDao, resolver);
  }
}
