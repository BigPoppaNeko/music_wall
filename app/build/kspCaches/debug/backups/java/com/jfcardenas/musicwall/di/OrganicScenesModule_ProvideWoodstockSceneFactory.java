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
public final class OrganicScenesModule_ProvideWoodstockSceneFactory implements Factory<OrganicRenderer> {
  private final Provider<Context> ctxProvider;

  private OrganicScenesModule_ProvideWoodstockSceneFactory(Provider<Context> ctxProvider) {
    this.ctxProvider = ctxProvider;
  }

  @Override
  public OrganicRenderer get() {
    return provideWoodstockScene(ctxProvider.get());
  }

  public static OrganicScenesModule_ProvideWoodstockSceneFactory create(
      Provider<Context> ctxProvider) {
    return new OrganicScenesModule_ProvideWoodstockSceneFactory(ctxProvider);
  }

  public static OrganicRenderer provideWoodstockScene(Context ctx) {
    return Preconditions.checkNotNullFromProvides(OrganicScenesModule.INSTANCE.provideWoodstockScene(ctx));
  }
}
