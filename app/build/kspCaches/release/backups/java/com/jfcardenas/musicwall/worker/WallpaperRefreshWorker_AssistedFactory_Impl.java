package com.jfcardenas.musicwall.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class WallpaperRefreshWorker_AssistedFactory_Impl implements WallpaperRefreshWorker_AssistedFactory {
  private final WallpaperRefreshWorker_Factory delegateFactory;

  WallpaperRefreshWorker_AssistedFactory_Impl(WallpaperRefreshWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public WallpaperRefreshWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<WallpaperRefreshWorker_AssistedFactory> create(
      WallpaperRefreshWorker_Factory delegateFactory) {
    return InstanceFactory.create(new WallpaperRefreshWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<WallpaperRefreshWorker_AssistedFactory> createFactoryProvider(
      WallpaperRefreshWorker_Factory delegateFactory) {
    return InstanceFactory.create(new WallpaperRefreshWorker_AssistedFactory_Impl(delegateFactory));
  }
}
