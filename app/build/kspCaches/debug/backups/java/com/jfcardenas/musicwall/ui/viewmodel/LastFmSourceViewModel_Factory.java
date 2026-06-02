package com.jfcardenas.musicwall.ui.viewmodel;

import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.data.UserSettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class LastFmSourceViewModel_Factory implements Factory<LastFmSourceViewModel> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private final Provider<UserSettingsRepository> settingsProvider;

  private LastFmSourceViewModel_Factory(Provider<LastFmService> lastFmServiceProvider,
      Provider<UserSettingsRepository> settingsProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
    this.settingsProvider = settingsProvider;
  }

  @Override
  public LastFmSourceViewModel get() {
    return newInstance(lastFmServiceProvider.get(), settingsProvider.get());
  }

  public static LastFmSourceViewModel_Factory create(Provider<LastFmService> lastFmServiceProvider,
      Provider<UserSettingsRepository> settingsProvider) {
    return new LastFmSourceViewModel_Factory(lastFmServiceProvider, settingsProvider);
  }

  public static LastFmSourceViewModel newInstance(LastFmService lastFmService,
      UserSettingsRepository settings) {
    return new LastFmSourceViewModel(lastFmService, settings);
  }
}
