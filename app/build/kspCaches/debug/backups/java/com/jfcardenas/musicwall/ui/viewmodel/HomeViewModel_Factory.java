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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<Context> contextProvider;

  private HomeViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<Context> contextProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(lastFmServiceProvider.get(), contextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<Context> contextProvider) {
    return new HomeViewModel_Factory(lastFmServiceProvider, contextProvider);
  }

  public static HomeViewModel newInstance(LastFmService lastFmService, Context context) {
    return new HomeViewModel(lastFmService, context);
  }
}
