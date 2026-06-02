package com.jfcardenas.musicwall.ui.viewmodel;

import com.jfcardenas.musicwall.data.local.db.dao.MuralDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class MuralHistoryViewModel_Factory implements Factory<MuralHistoryViewModel> {
  private final Provider<MuralDao> muralDaoProvider;

  private MuralHistoryViewModel_Factory(Provider<MuralDao> muralDaoProvider) {
    this.muralDaoProvider = muralDaoProvider;
  }

  @Override
  public MuralHistoryViewModel get() {
    return newInstance(muralDaoProvider.get());
  }

  public static MuralHistoryViewModel_Factory create(Provider<MuralDao> muralDaoProvider) {
    return new MuralHistoryViewModel_Factory(muralDaoProvider);
  }

  public static MuralHistoryViewModel newInstance(MuralDao muralDao) {
    return new MuralHistoryViewModel(muralDao);
  }
}
