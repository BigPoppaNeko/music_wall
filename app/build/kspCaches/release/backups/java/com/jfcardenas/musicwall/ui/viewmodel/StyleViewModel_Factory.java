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
public final class StyleViewModel_Factory implements Factory<StyleViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<Context> contextProvider;

  private StyleViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<Context> contextProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public StyleViewModel get() {
    return newInstance(lastFmServiceProvider.get(), contextProvider.get());
  }

  public static StyleViewModel_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<Context> contextProvider) {
    return new StyleViewModel_Factory(lastFmServiceProvider, contextProvider);
  }

  public static StyleViewModel newInstance(LastFmService lastFmService, Context context) {
    return new StyleViewModel(lastFmService, context);
  }
}
