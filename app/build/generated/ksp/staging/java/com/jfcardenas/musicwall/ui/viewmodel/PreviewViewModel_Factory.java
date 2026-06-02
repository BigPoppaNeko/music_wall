package com.jfcardenas.musicwall.ui.viewmodel;

import android.content.Context;
import androidx.lifecycle.SavedStateHandle;
import com.jfcardenas.musicwall.data.local.db.dao.MuralDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class PreviewViewModel_Factory implements Factory<PreviewViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<MuralDao> muralDaoProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private PreviewViewModel_Factory(Provider<Context> contextProvider,
      Provider<MuralDao> muralDaoProvider, Provider<SavedStateHandle> savedStateHandleProvider) {
    this.contextProvider = contextProvider;
    this.muralDaoProvider = muralDaoProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public PreviewViewModel get() {
    return newInstance(contextProvider.get(), muralDaoProvider.get(), savedStateHandleProvider.get());
  }

  public static PreviewViewModel_Factory create(Provider<Context> contextProvider,
      Provider<MuralDao> muralDaoProvider, Provider<SavedStateHandle> savedStateHandleProvider) {
    return new PreviewViewModel_Factory(contextProvider, muralDaoProvider, savedStateHandleProvider);
  }

  public static PreviewViewModel newInstance(Context context, MuralDao muralDao,
      SavedStateHandle savedStateHandle) {
    return new PreviewViewModel(context, muralDao, savedStateHandle);
  }
}
