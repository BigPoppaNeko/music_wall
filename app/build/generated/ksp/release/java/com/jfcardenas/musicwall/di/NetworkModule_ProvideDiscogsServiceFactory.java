package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.DiscogsService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideDiscogsServiceFactory implements Factory<DiscogsService> {
  @Override
  public DiscogsService get() {
    return provideDiscogsService();
  }

  public static NetworkModule_ProvideDiscogsServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DiscogsService provideDiscogsService() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideDiscogsService());
  }

  private static final class InstanceHolder {
    static final NetworkModule_ProvideDiscogsServiceFactory INSTANCE = new NetworkModule_ProvideDiscogsServiceFactory();
  }
}
