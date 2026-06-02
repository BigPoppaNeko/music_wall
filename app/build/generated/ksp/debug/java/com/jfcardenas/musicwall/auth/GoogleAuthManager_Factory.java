package com.jfcardenas.musicwall.auth;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class GoogleAuthManager_Factory implements Factory<GoogleAuthManager> {
  private final Provider<Context> contextProvider;

  private final Provider<UserSessionRepository> userSessionProvider;

  private GoogleAuthManager_Factory(Provider<Context> contextProvider,
      Provider<UserSessionRepository> userSessionProvider) {
    this.contextProvider = contextProvider;
    this.userSessionProvider = userSessionProvider;
  }

  @Override
  public GoogleAuthManager get() {
    return newInstance(contextProvider.get(), userSessionProvider.get());
  }

  public static GoogleAuthManager_Factory create(Provider<Context> contextProvider,
      Provider<UserSessionRepository> userSessionProvider) {
    return new GoogleAuthManager_Factory(contextProvider, userSessionProvider);
  }

  public static GoogleAuthManager newInstance(Context context, UserSessionRepository userSession) {
    return new GoogleAuthManager(context, userSession);
  }
}
