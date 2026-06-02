package com.jfcardenas.musicwall.service;

import com.jfcardenas.musicwall.auth.UserSessionRepository;
import com.jfcardenas.musicwall.scrobble.NowPlayingBus;
import com.jfcardenas.musicwall.scrobble.NowPlayingReader;
import com.jfcardenas.musicwall.scrobble.ScrobbleEngine;
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
public final class ScrobbleForegroundService_MembersInjector implements MembersInjector<ScrobbleForegroundService> {
  private final Provider<NowPlayingReader> nowPlayingReaderProvider;

  private final Provider<ScrobbleEngine> scrobbleEngineProvider;

  private final Provider<UserSessionRepository> userSessionProvider;

  private final Provider<NowPlayingBus> nowPlayingBusProvider;

  private ScrobbleForegroundService_MembersInjector(
      Provider<NowPlayingReader> nowPlayingReaderProvider,
      Provider<ScrobbleEngine> scrobbleEngineProvider,
      Provider<UserSessionRepository> userSessionProvider,
      Provider<NowPlayingBus> nowPlayingBusProvider) {
    this.nowPlayingReaderProvider = nowPlayingReaderProvider;
    this.scrobbleEngineProvider = scrobbleEngineProvider;
    this.userSessionProvider = userSessionProvider;
    this.nowPlayingBusProvider = nowPlayingBusProvider;
  }

  @Override
  public void injectMembers(ScrobbleForegroundService instance) {
    injectNowPlayingReader(instance, nowPlayingReaderProvider.get());
    injectScrobbleEngine(instance, scrobbleEngineProvider.get());
    injectUserSession(instance, userSessionProvider.get());
    injectNowPlayingBus(instance, nowPlayingBusProvider.get());
  }

  public static MembersInjector<ScrobbleForegroundService> create(
      Provider<NowPlayingReader> nowPlayingReaderProvider,
      Provider<ScrobbleEngine> scrobbleEngineProvider,
      Provider<UserSessionRepository> userSessionProvider,
      Provider<NowPlayingBus> nowPlayingBusProvider) {
    return new ScrobbleForegroundService_MembersInjector(nowPlayingReaderProvider, scrobbleEngineProvider, userSessionProvider, nowPlayingBusProvider);
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.ScrobbleForegroundService.nowPlayingReader")
  public static void injectNowPlayingReader(ScrobbleForegroundService instance,
      NowPlayingReader nowPlayingReader) {
    instance.nowPlayingReader = nowPlayingReader;
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.ScrobbleForegroundService.scrobbleEngine")
  public static void injectScrobbleEngine(ScrobbleForegroundService instance,
      ScrobbleEngine scrobbleEngine) {
    instance.scrobbleEngine = scrobbleEngine;
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.ScrobbleForegroundService.userSession")
  public static void injectUserSession(ScrobbleForegroundService instance,
      UserSessionRepository userSession) {
    instance.userSession = userSession;
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.service.ScrobbleForegroundService.nowPlayingBus")
  public static void injectNowPlayingBus(ScrobbleForegroundService instance,
      NowPlayingBus nowPlayingBus) {
    instance.nowPlayingBus = nowPlayingBus;
  }
}
