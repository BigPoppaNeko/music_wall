package com.jfcardenas.musicwall.features.connect;

import com.jfcardenas.musicwall.api.LastFmService;
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
public final class ConnectActivity_MembersInjector implements MembersInjector<ConnectActivity> {
  private final Provider<LastFmService> lastFmServiceProvider;

  private ConnectActivity_MembersInjector(Provider<LastFmService> lastFmServiceProvider) {
    this.lastFmServiceProvider = lastFmServiceProvider;
  }

  @Override
  public void injectMembers(ConnectActivity instance) {
    injectLastFmService(instance, lastFmServiceProvider.get());
  }

  public static MembersInjector<ConnectActivity> create(
      Provider<LastFmService> lastFmServiceProvider) {
    return new ConnectActivity_MembersInjector(lastFmServiceProvider);
  }

  @InjectedFieldSignature("com.jfcardenas.musicwall.features.connect.ConnectActivity.lastFmService")
  public static void injectLastFmService(ConnectActivity instance, LastFmService lastFmService) {
    instance.lastFmService = lastFmService;
  }
}
