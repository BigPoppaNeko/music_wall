package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.DeezerService;
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
public final class NetworkModule_ProvideDeezerServiceFactory implements Factory<DeezerService> {
  @Override
  public DeezerService get() {
    return provideDeezerService();
  }

  public static NetworkModule_ProvideDeezerServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DeezerService provideDeezerService() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideDeezerService());
  }

  private static final class InstanceHolder {
    static final NetworkModule_ProvideDeezerServiceFactory INSTANCE = new NetworkModule_ProvideDeezerServiceFactory();
  }
}
