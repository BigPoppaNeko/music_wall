package com.jfcardenas.musicwall.di;

import android.content.Context;
import com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "javax.inject.Named",
    "dagger.hilt.android.qualifiers.ApplicationContext"
})
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
public final class OrganicScenesModule_ProvideBritrockSceneFactory implements Factory<OrganicRenderer> {
  private final Provider<Context> ctxProvider;

  private OrganicScenesModule_ProvideBritrockSceneFactory(Provider<Context> ctxProvider) {
    this.ctxProvider = ctxProvider;
  }

  @Override
  public OrganicRenderer get() {
    return provideBritrockScene(ctxProvider.get());
  }

  public static OrganicScenesModule_ProvideBritrockSceneFactory create(
      Provider<Context> ctxProvider) {
    return new OrganicScenesModule_ProvideBritrockSceneFactory(ctxProvider);
  }

  public static OrganicRenderer provideBritrockScene(Context ctx) {
    return Preconditions.checkNotNullFromProvides(OrganicScenesModule.INSTANCE.provideBritrockScene(ctx));
  }
}
