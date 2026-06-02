package com.jfcardenas.musicwall.scrobble;

import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.data.local.db.dao.LfMatchCacheDao;
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
public final class LastFmTrackResolver_Factory implements Factory<LastFmTrackResolver> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<LfMatchCacheDao> matchCacheDaoProvider;

  private LastFmTrackResolver_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<LfMatchCacheDao> matchCacheDaoProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.matchCacheDaoProvider = matchCacheDaoProvider;
  }

  @Override
  public LastFmTrackResolver get() {
    return newInstance(lastFmServiceProvider.get(), matchCacheDaoProvider.get());
  }

  public static LastFmTrackResolver_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<LfMatchCacheDao> matchCacheDaoProvider) {
    return new LastFmTrackResolver_Factory(lastFmServiceProvider, matchCacheDaoProvider);
  }

  public static LastFmTrackResolver newInstance(LastFmService lastFmService,
      LfMatchCacheDao matchCacheDao) {
    return new LastFmTrackResolver(lastFmService, matchCacheDao);
  }
}
