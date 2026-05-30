package com.jfcardenas.musicwall.ui.viewmodel;

import android.content.Context;
import com.jfcardenas.musicwall.api.LastFmService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class LastFmSourceViewModel_Factory implements Factory<LastFmSourceViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<Context> contextProvider;

  private LastFmSourceViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<Context> contextProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public LastFmSourceViewModel get() {
    return newInstance(lastFmServiceProvider.get(), contextProvider.get());
  }

  public static LastFmSourceViewModel_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<Context> contextProvider) {
    return new LastFmSourceViewModel_Factory(lastFmServiceProvider, contextProvider);
  }

  public static LastFmSourceViewModel newInstance(LastFmService lastFmService, Context context) {
    return new LastFmSourceViewModel(lastFmService, context);
  }
}
