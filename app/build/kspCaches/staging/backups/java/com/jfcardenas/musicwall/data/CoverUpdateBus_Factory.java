package com.jfcardenas.musicwall.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class CoverUpdateBus_Factory implements Factory<CoverUpdateBus> {
  @Override
  public CoverUpdateBus get() {
    return newInstance();
  }

  public static CoverUpdateBus_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CoverUpdateBus newInstance() {
    return new CoverUpdateBus();
  }

  private static final class InstanceHolder {
    static final CoverUpdateBus_Factory INSTANCE = new CoverUpdateBus_Factory();
  }
}
