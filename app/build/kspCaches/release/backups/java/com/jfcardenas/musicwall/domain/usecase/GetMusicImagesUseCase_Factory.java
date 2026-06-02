package com.jfcardenas.musicwall.domain.usecase;

import com.jfcardenas.musicwall.domain.repository.MusicRepository;
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
public final class GetMusicImagesUseCase_Factory implements Factory<GetMusicImagesUseCase> {
  private final Provider<MusicRepository> repositoryProvider;

  private GetMusicImagesUseCase_Factory(Provider<MusicRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetMusicImagesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetMusicImagesUseCase_Factory create(Provider<MusicRepository> repositoryProvider) {
    return new GetMusicImagesUseCase_Factory(repositoryProvider);
  }

  public static GetMusicImagesUseCase newInstance(MusicRepository repository) {
    return new GetMusicImagesUseCase(repository);
  }
}
