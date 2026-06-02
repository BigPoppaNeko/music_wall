package com.jfcardenas.musicwall.ui.viewmodel;

import com.jfcardenas.musicwall.auth.UserSessionRepository;
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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<UserSessionRepository> userSessionProvider;

  private OnboardingViewModel_Factory(Provider<UserSessionRepository> userSessionProvider) {
    this.userSessionProvider = userSessionProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(userSessionProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<UserSessionRepository> userSessionProvider) {
    return new OnboardingViewModel_Factory(userSessionProvider);
  }

  public static OnboardingViewModel newInstance(UserSessionRepository userSession) {
    return new OnboardingViewModel(userSession);
  }
}
