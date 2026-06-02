package com.jfcardenas.musicwall.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
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
public final class WallpaperRefreshWorker_Factory {
  public WallpaperRefreshWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params);
  }

  public static WallpaperRefreshWorker_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WallpaperRefreshWorker newInstance(Context context, WorkerParameters params) {
    return new WallpaperRefreshWorker(context, params);
  }

  private static final class InstanceHolder {
    static final WallpaperRefreshWorker_Factory INSTANCE = new WallpaperRefreshWorker_Factory();
  }
}
