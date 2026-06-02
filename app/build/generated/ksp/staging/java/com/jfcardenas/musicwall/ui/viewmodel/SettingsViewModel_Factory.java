package com.jfcardenas.musicwall.ui.viewmodel;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserSettingsRepository> settingsProvider;

  private SettingsViewModel_Factory(Provider<UserSettingsRepository> settingsProvider) {
    this.settingsProvider = settingsProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<UserSettingsRepository> settingsProvider) {
    return new SettingsViewModel_Factory(settingsProvider);
  }

  public static SettingsViewModel newInstance(UserSettingsRepository settings) {
    return new SettingsViewModel(settings);
  }
}
