package com.jfcardenas.musicwall.scrobble;

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
public final class ScrobbleEngine_Factory implements Factory<ScrobbleEngine> {
  private final Provider<ScrobbleRepository> repositoryProvider;

  private ScrobbleEngine_Factory(Provider<ScrobbleRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScrobbleEngine get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScrobbleEngine_Factory create(Provider<ScrobbleRepository> repositoryProvider) {
    return new ScrobbleEngine_Factory(repositoryProvider);
  }

  public static ScrobbleEngine newInstance(ScrobbleRepository repository) {
    return new ScrobbleEngine(repository);
  }
}
