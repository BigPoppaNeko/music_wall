package com.jfcardenas.musicwall.service;

import com.jfcardenas.musicwall.scrobble.NowPlayingBus;
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
public final class NowPlayingListenerService_MembersInjector implements MembersInjector<NowPlayingListenerService> {
  private final Provider<NowPlayingBus> nowPlayingBusProvider;

  private NowPlayingListenerService_MembersInjector(Provider<NowPlayingBus> nowPlayingBusProvider) {
    this.nowPlayingBusProvider = nowPlayingBusProvider;
  }

  @Override
  public void injectMembers(NowPlayingListenerService instance) {
    injectNowPlayingBus(instance, nowPlayingBusProvider.get());
  }

  public static MembersInjector<NowPlayingListenerService> create(
      Provider<NowPlayingBus> nowPlayingBusProvider) {
    return new NowPlayingListenerService_MembersInjector(nowPlayingBusProvider);
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.NowPlayingListenerService.nowPlayingBus")
  public static void injectNowPlayingBus(NowPlayingListenerService instance,
      NowPlayingBus nowPlayingBus) {
    instance.nowPlayingBus = nowPlayingBus;
  }
}
