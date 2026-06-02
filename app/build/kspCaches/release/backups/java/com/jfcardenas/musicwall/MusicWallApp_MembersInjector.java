package com.jfcardenas.musicwall;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MusicWallApp_MembersInjector implements MembersInjector<MusicWallApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private MusicWallApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  @Override
  public void injectMembers(MusicWallApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  public static MembersInjector<MusicWallApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new MusicWallApp_MembersInjector(workerFactoryProvider);
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.MusicWallApp.workerFactory")
  public static void injectWorkerFactory(MusicWallApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
