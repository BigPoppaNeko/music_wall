package com.jfcardenas.musicwall.scrobble;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NowPlayingReader_Factory implements Factory<NowPlayingReader> {
  private final Provider<Context> contextProvider;

  private NowPlayingReader_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NowPlayingReader get() {
    return newInstance(contextProvider.get());
  }

  public static NowPlayingReader_Factory create(Provider<Context> contextProvider) {
    return new NowPlayingReader_Factory(contextProvider);
  }

  public static NowPlayingReader newInstance(Context context) {
    return new NowPlayingReader(context);
  }
}
