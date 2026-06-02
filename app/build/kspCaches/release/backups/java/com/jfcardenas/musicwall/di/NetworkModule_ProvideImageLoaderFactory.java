package com.jfcardenas.musicwall.di;

import android.content.Context;
import coil.ImageLoader;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class NetworkModule_ProvideImageLoaderFactory implements Factory<ImageLoader> {
  private final Provider<Context> contextProvider;

  private NetworkModule_ProvideImageLoaderFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ImageLoader get() {
    return provideImageLoader(contextProvider.get());
  }

  public static NetworkModule_ProvideImageLoaderFactory create(Provider<Context> contextProvider) {
    return new NetworkModule_ProvideImageLoaderFactory(contextProvider);
  }

  public static ImageLoader provideImageLoader(Context context) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideImageLoader(context));
  }
}
