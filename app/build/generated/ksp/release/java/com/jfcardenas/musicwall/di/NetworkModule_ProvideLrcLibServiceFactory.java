package com.jfcardenas.musicwall.di;

import com.jfcardenas.musicwall.api.LrcLibService;
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
public final class NetworkModule_ProvideLrcLibServiceFactory implements Factory<LrcLibService> {
  @Override
  public LrcLibService get() {
    return provideLrcLibService();
  }

  public static NetworkModule_ProvideLrcLibServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LrcLibService provideLrcLibService() {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideLrcLibService());
  }

  private static final class InstanceHolder {
    static final NetworkModule_ProvideLrcLibServiceFactory INSTANCE = new NetworkModule_ProvideLrcLibServiceFactory();
  }
}
