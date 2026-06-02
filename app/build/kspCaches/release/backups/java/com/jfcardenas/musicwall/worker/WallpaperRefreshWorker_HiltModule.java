package com.jfcardenas.musicwall.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = WallpaperRefreshWorker.class
)
public interface WallpaperRefreshWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.jfcardenas.musicwall.worker.WallpaperRefreshWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(
      WallpaperRefreshWorker_AssistedFactory factory);
}
