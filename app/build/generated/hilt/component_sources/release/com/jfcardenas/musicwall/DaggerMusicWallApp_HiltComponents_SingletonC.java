package com.jfcardenas.musicwall;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import coil.ImageLoader;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.jfcardenas.musicwall.api.DeezerService;
import com.jfcardenas.musicwall.api.DiscogsService;
import com.jfcardenas.musicwall.api.LastFmService;
import com.jfcardenas.musicwall.api.LrcLibService;
import com.jfcardenas.musicwall.api.LyricsService;
import com.jfcardenas.musicwall.api.SpotifyOEmbedService;
import com.jfcardenas.musicwall.auth.GoogleAuthManager;
import com.jfcardenas.musicwall.auth.UserSessionRepository;
import com.jfcardenas.musicwall.data.ArtistImageResolver;
import com.jfcardenas.musicwall.data.CoverFallbackRepository;
import com.jfcardenas.musicwall.data.CoverUpdateBus;
import com.jfcardenas.musicwall.data.LastFmRepository;
import com.jfcardenas.musicwall.data.local.db.MusicWallDatabase;
import com.jfcardenas.musicwall.data.local.db.dao.AlbumDao;
import com.jfcardenas.musicwall.data.local.db.dao.ArtistDao;
import com.jfcardenas.musicwall.data.local.db.dao.FavoriteAlbumDao;
import com.jfcardenas.musicwall.data.local.db.dao.LfMatchCacheDao;
import com.jfcardenas.musicwall.data.local.db.dao.LocalScrobbleDao;
import com.jfcardenas.musicwall.data.local.db.dao.MuralDao;
import com.jfcardenas.musicwall.data.local.db.dao.TrackDao;
import com.jfcardenas.musicwall.data.local.db.dao.VetoedAlbumDao;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideAlbumDaoFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideArtistDaoFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideDatabaseFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideFavoriteAlbumDaoFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideLfMatchCacheDaoFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideLocalScrobbleDaoFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideMuralDaoFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideTrackDaoFactory;
import com.jfcardenas.musicwall.di.DatabaseModule_ProvideVetoedAlbumDaoFactory;
import com.jfcardenas.musicwall.di.NetworkModule_ProvideDeezerServiceFactory;
import com.jfcardenas.musicwall.di.NetworkModule_ProvideDiscogsServiceFactory;
import com.jfcardenas.musicwall.di.NetworkModule_ProvideImageLoaderFactory;
import com.jfcardenas.musicwall.di.NetworkModule_ProvideLastFmServiceFactory;
import com.jfcardenas.musicwall.di.NetworkModule_ProvideLrcLibServiceFactory;
import com.jfcardenas.musicwall.di.NetworkModule_ProvideLyricsServiceFactory;
import com.jfcardenas.musicwall.di.OrganicScenesModule_ProvideBanoBarLimaSceneFactory;
import com.jfcardenas.musicwall.di.OrganicScenesModule_ProvideBritrockSceneFactory;
import com.jfcardenas.musicwall.di.OrganicScenesModule_ProvideWoodstockSceneFactory;
import com.jfcardenas.musicwall.di.SpotifyModule_ProvideSpotifyOEmbedServiceFactory;
import com.jfcardenas.musicwall.domain.usecase.GetMusicImagesUseCase;
import com.jfcardenas.musicwall.features.connect.ConnectActivity;
import com.jfcardenas.musicwall.features.connect.ConnectActivity_MembersInjector;
import com.jfcardenas.musicwall.features.wallpaper.renderer.ManchesterWallRenderer;
import com.jfcardenas.musicwall.features.wallpaper.renderer.MosaicBlendRenderer;
import com.jfcardenas.musicwall.features.wallpaper.renderer.PsychedelicGridRenderer;
import com.jfcardenas.musicwall.features.wallpaper.renderer.PuzzleRenderer;
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRendererFactory;
import com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer;
import com.jfcardenas.musicwall.scrobble.LastFmTrackResolver;
import com.jfcardenas.musicwall.scrobble.NowPlayingBus;
import com.jfcardenas.musicwall.scrobble.NowPlayingReader;
import com.jfcardenas.musicwall.scrobble.ScrobbleEngine;
import com.jfcardenas.musicwall.scrobble.ScrobbleRepository;
import com.jfcardenas.musicwall.service.CollageWallpaper;
import com.jfcardenas.musicwall.service.CollageWallpaper_MembersInjector;
import com.jfcardenas.musicwall.service.NowPlayingListenerService;
import com.jfcardenas.musicwall.service.NowPlayingListenerService_MembersInjector;
import com.jfcardenas.musicwall.service.ScrobbleForegroundService;
import com.jfcardenas.musicwall.service.ScrobbleForegroundService_MembersInjector;
import com.jfcardenas.musicwall.settings.WallpaperSettingsActivity;
import com.jfcardenas.musicwall.settings.WallpaperSettingsActivity_MembersInjector;
import com.jfcardenas.musicwall.ui.viewmodel.ArtistasViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.ArtistasViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.ArtistasViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.ArtistasViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.ExploreViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.ExploreViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.ExploreViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.ExploreViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.FavoritesViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.FavoritesViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.FavoritesViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.FavoritesViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.GeneratingViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.GeneratingViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.GeneratingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.GeneratingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.LastFmSourceViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.LastFmSourceViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.LastFmSourceViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.LastFmSourceViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.MuralHistoryViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.MuralHistoryViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.MuralHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.MuralHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.NowPlayingDetailViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.NowPlayingDetailViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.NowPlayingDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.NowPlayingDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.OnboardingViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.OnboardingViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.PreviewViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.PreviewViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.PreviewViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.PreviewViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.SpotifySourceViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.SpotifySourceViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.SpotifySourceViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.SpotifySourceViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.StyleViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.StyleViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.StyleViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.StyleViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.WallSceneSetupViewModel;
import com.jfcardenas.musicwall.ui.viewmodel.WallSceneSetupViewModel_HiltModules;
import com.jfcardenas.musicwall.ui.viewmodel.WallSceneSetupViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.jfcardenas.musicwall.ui.viewmodel.WallSceneSetupViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.jfcardenas.musicwall.worker.WallpaperRefreshWorker;
import com.jfcardenas.musicwall.worker.WallpaperRefreshWorker_AssistedFactory;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerMusicWallApp_HiltComponents_SingletonC {
  private DaggerMusicWallApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public MusicWallApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements MusicWallApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public MusicWallApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements MusicWallApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public MusicWallApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements MusicWallApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public MusicWallApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements MusicWallApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MusicWallApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements MusicWallApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MusicWallApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements MusicWallApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public MusicWallApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements MusicWallApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public MusicWallApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends MusicWallApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends MusicWallApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends MusicWallApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends MusicWallApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    GetMusicImagesUseCase getMusicImagesUseCase() {
      return new GetMusicImagesUseCase(singletonCImpl.lastFmRepositoryProvider.get());
    }

    Map keySetMapOfClassOfAndBooleanBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, Boolean>newMapBuilder(14);
      mapBuilder.put(ArtistasViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArtistasViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(CoverInteractionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CoverInteractionViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(ExploreViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ExploreViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(FavoritesViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, FavoritesViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(GeneratingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GeneratingViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(HomeViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HomeViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(LastFmSourceViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LastFmSourceViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(MuralHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MuralHistoryViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(NowPlayingDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, NowPlayingDetailViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, OnboardingViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(PreviewViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PreviewViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(SpotifySourceViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SpotifySourceViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(StyleViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, StyleViewModel_HiltModules.KeyModule.provide());
      mapBuilder.put(WallSceneSetupViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, WallSceneSetupViewModel_HiltModules.KeyModule.provide());
      return mapBuilder.build();
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public void injectOnboardingActivity(OnboardingActivity onboardingActivity) {
    }

    @Override
    public void injectConnectActivity(ConnectActivity connectActivity) {
      injectConnectActivity2(connectActivity);
    }

    @Override
    public void injectWallpaperSettingsActivity(
        WallpaperSettingsActivity wallpaperSettingsActivity) {
      injectWallpaperSettingsActivity2(wallpaperSettingsActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(keySetMapOfClassOfAndBooleanBuilder());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @CanIgnoreReturnValue
    private ConnectActivity injectConnectActivity2(ConnectActivity instance) {
      ConnectActivity_MembersInjector.injectLastFmService(instance, singletonCImpl.provideLastFmServiceProvider.get());
      return instance;
    }

    @CanIgnoreReturnValue
    private WallpaperSettingsActivity injectWallpaperSettingsActivity2(
        WallpaperSettingsActivity instance2) {
      WallpaperSettingsActivity_MembersInjector.injectGetMusicImages(instance2, getMusicImagesUseCase());
      WallpaperSettingsActivity_MembersInjector.injectImageLoader(instance2, singletonCImpl.provideImageLoaderProvider.get());
      WallpaperSettingsActivity_MembersInjector.injectRendererFactory(instance2, singletonCImpl.wallpaperRendererFactoryProvider.get());
      return instance2;
    }
  }

  private static final class ViewModelCImpl extends MusicWallApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<ArtistasViewModel> artistasViewModelProvider;

    Provider<CoverInteractionViewModel> coverInteractionViewModelProvider;

    Provider<ExploreViewModel> exploreViewModelProvider;

    Provider<FavoritesViewModel> favoritesViewModelProvider;

    Provider<GeneratingViewModel> generatingViewModelProvider;

    Provider<HomeViewModel> homeViewModelProvider;

    Provider<LastFmSourceViewModel> lastFmSourceViewModelProvider;

    Provider<MuralHistoryViewModel> muralHistoryViewModelProvider;

    Provider<NowPlayingDetailViewModel> nowPlayingDetailViewModelProvider;

    Provider<OnboardingViewModel> onboardingViewModelProvider;

    Provider<PreviewViewModel> previewViewModelProvider;

    Provider<SpotifySourceViewModel> spotifySourceViewModelProvider;

    Provider<StyleViewModel> styleViewModelProvider;

    Provider<WallSceneSetupViewModel> wallSceneSetupViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    GetMusicImagesUseCase getMusicImagesUseCase() {
      return new GetMusicImagesUseCase(singletonCImpl.lastFmRepositoryProvider.get());
    }

    Map hiltViewModelMapMapOfClassOfAndProviderOfViewModelBuilder() {
      MapBuilder mapBuilder = MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(14);
      mapBuilder.put(ArtistasViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (artistasViewModelProvider)));
      mapBuilder.put(CoverInteractionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (coverInteractionViewModelProvider)));
      mapBuilder.put(ExploreViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (exploreViewModelProvider)));
      mapBuilder.put(FavoritesViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (favoritesViewModelProvider)));
      mapBuilder.put(GeneratingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (generatingViewModelProvider)));
      mapBuilder.put(HomeViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (homeViewModelProvider)));
      mapBuilder.put(LastFmSourceViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (lastFmSourceViewModelProvider)));
      mapBuilder.put(MuralHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (muralHistoryViewModelProvider)));
      mapBuilder.put(NowPlayingDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (nowPlayingDetailViewModelProvider)));
      mapBuilder.put(OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (onboardingViewModelProvider)));
      mapBuilder.put(PreviewViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (previewViewModelProvider)));
      mapBuilder.put(SpotifySourceViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (spotifySourceViewModelProvider)));
      mapBuilder.put(StyleViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (styleViewModelProvider)));
      mapBuilder.put(WallSceneSetupViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (wallSceneSetupViewModelProvider)));
      return mapBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.artistasViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.coverInteractionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.exploreViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.favoritesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.generatingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.lastFmSourceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.muralHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.nowPlayingDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.onboardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.previewViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.spotifySourceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.styleViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.wallSceneSetupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(hiltViewModelMapMapOfClassOfAndProviderOfViewModelBuilder());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.jfcardenas.musicwall.ui.viewmodel.ArtistasViewModel
          return (T) new ArtistasViewModel(singletonCImpl.provideLastFmServiceProvider.get(), singletonCImpl.provideDeezerServiceProvider.get(), singletonCImpl.coverFallbackRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.jfcardenas.musicwall.ui.viewmodel.CoverInteractionViewModel
          return (T) new CoverInteractionViewModel(singletonCImpl.provideLastFmServiceProvider.get(), singletonCImpl.favoriteAlbumDao());

          case 2: // com.jfcardenas.musicwall.ui.viewmodel.ExploreViewModel
          return (T) new ExploreViewModel(singletonCImpl.provideLastFmServiceProvider.get());

          case 3: // com.jfcardenas.musicwall.ui.viewmodel.FavoritesViewModel
          return (T) new FavoritesViewModel(singletonCImpl.favoriteAlbumDao());

          case 4: // com.jfcardenas.musicwall.ui.viewmodel.GeneratingViewModel
          return (T) new GeneratingViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), viewModelCImpl.getMusicImagesUseCase(), singletonCImpl.wallpaperRendererFactoryProvider.get(), singletonCImpl.provideImageLoaderProvider.get(), viewModelCImpl.savedStateHandle);

          case 5: // com.jfcardenas.musicwall.ui.viewmodel.HomeViewModel
          return (T) new HomeViewModel(singletonCImpl.provideLastFmServiceProvider.get(), singletonCImpl.albumDao(), singletonCImpl.favoriteAlbumDao(), singletonCImpl.vetoedAlbumDao(), singletonCImpl.coverFallbackRepositoryProvider.get(), singletonCImpl.coverUpdateBusProvider.get(), singletonCImpl.userSessionRepositoryProvider.get(), singletonCImpl.scrobbleRepositoryProvider.get(), singletonCImpl.nowPlayingBusProvider.get(), viewModelCImpl.getMusicImagesUseCase(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.jfcardenas.musicwall.ui.viewmodel.LastFmSourceViewModel
          return (T) new LastFmSourceViewModel(singletonCImpl.provideLastFmServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.jfcardenas.musicwall.ui.viewmodel.MuralHistoryViewModel
          return (T) new MuralHistoryViewModel(singletonCImpl.muralDao());

          case 8: // com.jfcardenas.musicwall.ui.viewmodel.NowPlayingDetailViewModel
          return (T) new NowPlayingDetailViewModel(singletonCImpl.provideLastFmServiceProvider.get(), singletonCImpl.provideLyricsServiceProvider.get(), singletonCImpl.provideLrcLibServiceProvider.get(), singletonCImpl.provideDiscogsServiceProvider.get(), singletonCImpl.coverFallbackRepositoryProvider.get(), singletonCImpl.favoriteAlbumDao());

          case 9: // com.jfcardenas.musicwall.ui.viewmodel.OnboardingViewModel
          return (T) new OnboardingViewModel(singletonCImpl.userSessionRepositoryProvider.get());

          case 10: // com.jfcardenas.musicwall.ui.viewmodel.PreviewViewModel
          return (T) new PreviewViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.muralDao(), viewModelCImpl.savedStateHandle);

          case 11: // com.jfcardenas.musicwall.ui.viewmodel.SpotifySourceViewModel
          return (T) new SpotifySourceViewModel(singletonCImpl.provideSpotifyOEmbedServiceProvider.get());

          case 12: // com.jfcardenas.musicwall.ui.viewmodel.StyleViewModel
          return (T) new StyleViewModel(singletonCImpl.provideLastFmServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.jfcardenas.musicwall.ui.viewmodel.WallSceneSetupViewModel
          return (T) new WallSceneSetupViewModel(singletonCImpl.favoriteAlbumDao(), singletonCImpl.provideLastFmServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends MusicWallApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends MusicWallApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    GetMusicImagesUseCase getMusicImagesUseCase() {
      return new GetMusicImagesUseCase(singletonCImpl.lastFmRepositoryProvider.get());
    }

    @Override
    public void injectCollageWallpaper(CollageWallpaper collageWallpaper) {
      injectCollageWallpaper2(collageWallpaper);
    }

    @Override
    public void injectNowPlayingListenerService(
        NowPlayingListenerService nowPlayingListenerService) {
      injectNowPlayingListenerService2(nowPlayingListenerService);
    }

    @Override
    public void injectScrobbleForegroundService(
        ScrobbleForegroundService scrobbleForegroundService) {
      injectScrobbleForegroundService2(scrobbleForegroundService);
    }

    @CanIgnoreReturnValue
    private CollageWallpaper injectCollageWallpaper2(CollageWallpaper instance) {
      CollageWallpaper_MembersInjector.injectGetMusicImages(instance, getMusicImagesUseCase());
      CollageWallpaper_MembersInjector.injectRendererFactory(instance, singletonCImpl.wallpaperRendererFactoryProvider.get());
      CollageWallpaper_MembersInjector.injectImageLoader(instance, singletonCImpl.provideImageLoaderProvider.get());
      return instance;
    }

    @CanIgnoreReturnValue
    private NowPlayingListenerService injectNowPlayingListenerService2(
        NowPlayingListenerService instance2) {
      NowPlayingListenerService_MembersInjector.injectNowPlayingBus(instance2, singletonCImpl.nowPlayingBusProvider.get());
      return instance2;
    }

    @CanIgnoreReturnValue
    private ScrobbleForegroundService injectScrobbleForegroundService2(
        ScrobbleForegroundService instance3) {
      ScrobbleForegroundService_MembersInjector.injectNowPlayingReader(instance3, singletonCImpl.nowPlayingReaderProvider.get());
      ScrobbleForegroundService_MembersInjector.injectScrobbleEngine(instance3, singletonCImpl.scrobbleEngineProvider.get());
      ScrobbleForegroundService_MembersInjector.injectUserSession(instance3, singletonCImpl.userSessionRepositoryProvider.get());
      ScrobbleForegroundService_MembersInjector.injectNowPlayingBus(instance3, singletonCImpl.nowPlayingBusProvider.get());
      return instance3;
    }
  }

  private static final class SingletonCImpl extends MusicWallApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<WallpaperRefreshWorker_AssistedFactory> wallpaperRefreshWorker_AssistedFactoryProvider;

    Provider<UserSessionRepository> userSessionRepositoryProvider;

    Provider<GoogleAuthManager> googleAuthManagerProvider;

    Provider<LastFmService> provideLastFmServiceProvider;

    Provider<MusicWallDatabase> provideDatabaseProvider;

    Provider<ArtistImageResolver> artistImageResolverProvider;

    Provider<LastFmRepository> lastFmRepositoryProvider;

    Provider<ImageLoader> provideImageLoaderProvider;

    Provider<MosaicBlendRenderer> mosaicBlendRendererProvider;

    Provider<PuzzleRenderer> puzzleRendererProvider;

    Provider<PsychedelicGridRenderer> psychedelicGridRendererProvider;

    Provider<ManchesterWallRenderer> manchesterWallRendererProvider;

    Provider<OrganicRenderer> provideBritrockSceneProvider;

    Provider<OrganicRenderer> provideBanoBarLimaSceneProvider;

    Provider<OrganicRenderer> provideWoodstockSceneProvider;

    Provider<WallpaperRendererFactory> wallpaperRendererFactoryProvider;

    Provider<DeezerService> provideDeezerServiceProvider;

    Provider<CoverUpdateBus> coverUpdateBusProvider;

    Provider<CoverFallbackRepository> coverFallbackRepositoryProvider;

    Provider<LastFmTrackResolver> lastFmTrackResolverProvider;

    Provider<ScrobbleRepository> scrobbleRepositoryProvider;

    Provider<NowPlayingBus> nowPlayingBusProvider;

    Provider<LyricsService> provideLyricsServiceProvider;

    Provider<LrcLibService> provideLrcLibServiceProvider;

    Provider<DiscogsService> provideDiscogsServiceProvider;

    Provider<SpotifyOEmbedService> provideSpotifyOEmbedServiceProvider;

    Provider<NowPlayingReader> nowPlayingReaderProvider;

    Provider<ScrobbleEngine> scrobbleEngineProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);

    }

    Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return Collections.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>singletonMap("com.jfcardenas.musicwall.worker.WallpaperRefreshWorker", ((Provider) (wallpaperRefreshWorker_AssistedFactoryProvider)));
    }

    HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    AlbumDao albumDao() {
      return DatabaseModule_ProvideAlbumDaoFactory.provideAlbumDao(provideDatabaseProvider.get());
    }

    ArtistDao artistDao() {
      return DatabaseModule_ProvideArtistDaoFactory.provideArtistDao(provideDatabaseProvider.get());
    }

    TrackDao trackDao() {
      return DatabaseModule_ProvideTrackDaoFactory.provideTrackDao(provideDatabaseProvider.get());
    }

    FavoriteAlbumDao favoriteAlbumDao() {
      return DatabaseModule_ProvideFavoriteAlbumDaoFactory.provideFavoriteAlbumDao(provideDatabaseProvider.get());
    }

    VetoedAlbumDao vetoedAlbumDao() {
      return DatabaseModule_ProvideVetoedAlbumDaoFactory.provideVetoedAlbumDao(provideDatabaseProvider.get());
    }

    LocalScrobbleDao localScrobbleDao() {
      return DatabaseModule_ProvideLocalScrobbleDaoFactory.provideLocalScrobbleDao(provideDatabaseProvider.get());
    }

    LfMatchCacheDao lfMatchCacheDao() {
      return DatabaseModule_ProvideLfMatchCacheDaoFactory.provideLfMatchCacheDao(provideDatabaseProvider.get());
    }

    MuralDao muralDao() {
      return DatabaseModule_ProvideMuralDaoFactory.provideMuralDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.wallpaperRefreshWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<WallpaperRefreshWorker_AssistedFactory>(singletonCImpl, 0));
      this.userSessionRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserSessionRepository>(singletonCImpl, 1));
      this.googleAuthManagerProvider = DoubleCheck.provider(new SwitchingProvider<GoogleAuthManager>(singletonCImpl, 2));
      this.provideLastFmServiceProvider = DoubleCheck.provider(new SwitchingProvider<LastFmService>(singletonCImpl, 3));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<MusicWallDatabase>(singletonCImpl, 5));
      this.artistImageResolverProvider = DoubleCheck.provider(new SwitchingProvider<ArtistImageResolver>(singletonCImpl, 6));
      this.lastFmRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<LastFmRepository>(singletonCImpl, 4));
      this.provideImageLoaderProvider = DoubleCheck.provider(new SwitchingProvider<ImageLoader>(singletonCImpl, 7));
      this.mosaicBlendRendererProvider = DoubleCheck.provider(new SwitchingProvider<MosaicBlendRenderer>(singletonCImpl, 9));
      this.puzzleRendererProvider = DoubleCheck.provider(new SwitchingProvider<PuzzleRenderer>(singletonCImpl, 10));
      this.psychedelicGridRendererProvider = DoubleCheck.provider(new SwitchingProvider<PsychedelicGridRenderer>(singletonCImpl, 11));
      this.manchesterWallRendererProvider = DoubleCheck.provider(new SwitchingProvider<ManchesterWallRenderer>(singletonCImpl, 12));
      this.provideBritrockSceneProvider = DoubleCheck.provider(new SwitchingProvider<OrganicRenderer>(singletonCImpl, 13));
      this.provideBanoBarLimaSceneProvider = DoubleCheck.provider(new SwitchingProvider<OrganicRenderer>(singletonCImpl, 14));
      this.provideWoodstockSceneProvider = DoubleCheck.provider(new SwitchingProvider<OrganicRenderer>(singletonCImpl, 15));
      this.wallpaperRendererFactoryProvider = DoubleCheck.provider(new SwitchingProvider<WallpaperRendererFactory>(singletonCImpl, 8));
      this.provideDeezerServiceProvider = DoubleCheck.provider(new SwitchingProvider<DeezerService>(singletonCImpl, 16));
      this.coverUpdateBusProvider = DoubleCheck.provider(new SwitchingProvider<CoverUpdateBus>(singletonCImpl, 18));
      this.coverFallbackRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CoverFallbackRepository>(singletonCImpl, 17));
      this.lastFmTrackResolverProvider = DoubleCheck.provider(new SwitchingProvider<LastFmTrackResolver>(singletonCImpl, 20));
      this.scrobbleRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ScrobbleRepository>(singletonCImpl, 19));
      this.nowPlayingBusProvider = DoubleCheck.provider(new SwitchingProvider<NowPlayingBus>(singletonCImpl, 21));
      this.provideLyricsServiceProvider = DoubleCheck.provider(new SwitchingProvider<LyricsService>(singletonCImpl, 22));
      this.provideLrcLibServiceProvider = DoubleCheck.provider(new SwitchingProvider<LrcLibService>(singletonCImpl, 23));
      this.provideDiscogsServiceProvider = DoubleCheck.provider(new SwitchingProvider<DiscogsService>(singletonCImpl, 24));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.provideSpotifyOEmbedServiceProvider = DoubleCheck.provider(new SwitchingProvider<SpotifyOEmbedService>(singletonCImpl, 25));
      this.nowPlayingReaderProvider = DoubleCheck.provider(new SwitchingProvider<NowPlayingReader>(singletonCImpl, 26));
      this.scrobbleEngineProvider = DoubleCheck.provider(new SwitchingProvider<ScrobbleEngine>(singletonCImpl, 27));
    }

    @Override
    public void injectMusicWallApp(MusicWallApp musicWallApp) {
      injectMusicWallApp2(musicWallApp);
    }

    @Override
    public UserSessionRepository userSessionRepository() {
      return userSessionRepositoryProvider.get();
    }

    @Override
    public GoogleAuthManager googleAuthManager() {
      return googleAuthManagerProvider.get();
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private MusicWallApp injectMusicWallApp2(MusicWallApp instance) {
      MusicWallApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.jfcardenas.musicwall.worker.WallpaperRefreshWorker_AssistedFactory
          return (T) new WallpaperRefreshWorker_AssistedFactory() {
            @Override
            public WallpaperRefreshWorker create(Context context, WorkerParameters params) {
              return new WallpaperRefreshWorker(context, params);
            }
          };

          case 1: // com.jfcardenas.musicwall.auth.UserSessionRepository
          return (T) new UserSessionRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.jfcardenas.musicwall.auth.GoogleAuthManager
          return (T) new GoogleAuthManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.userSessionRepositoryProvider.get());

          case 3: // com.jfcardenas.musicwall.api.LastFmService
          return (T) NetworkModule_ProvideLastFmServiceFactory.provideLastFmService();

          case 4: // com.jfcardenas.musicwall.data.LastFmRepository
          return (T) new LastFmRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideLastFmServiceProvider.get(), singletonCImpl.albumDao(), singletonCImpl.artistDao(), singletonCImpl.trackDao(), singletonCImpl.artistImageResolverProvider.get());

          case 5: // com.jfcardenas.musicwall.data.local.db.MusicWallDatabase
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.jfcardenas.musicwall.data.ArtistImageResolver
          return (T) new ArtistImageResolver();

          case 7: // coil.ImageLoader
          return (T) NetworkModule_ProvideImageLoaderFactory.provideImageLoader(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRendererFactory
          return (T) new WallpaperRendererFactory(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.mosaicBlendRendererProvider.get(), singletonCImpl.puzzleRendererProvider.get(), singletonCImpl.psychedelicGridRendererProvider.get(), singletonCImpl.manchesterWallRendererProvider.get(), singletonCImpl.provideBritrockSceneProvider.get(), singletonCImpl.provideBanoBarLimaSceneProvider.get(), singletonCImpl.provideWoodstockSceneProvider.get());

          case 9: // com.jfcardenas.musicwall.features.wallpaper.renderer.MosaicBlendRenderer
          return (T) new MosaicBlendRenderer();

          case 10: // com.jfcardenas.musicwall.features.wallpaper.renderer.PuzzleRenderer
          return (T) new PuzzleRenderer();

          case 11: // com.jfcardenas.musicwall.features.wallpaper.renderer.PsychedelicGridRenderer
          return (T) new PsychedelicGridRenderer();

          case 12: // com.jfcardenas.musicwall.features.wallpaper.renderer.ManchesterWallRenderer
          return (T) new ManchesterWallRenderer();

          case 13: // @javax.inject.Named("scene_britrock") com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer
          return (T) OrganicScenesModule_ProvideBritrockSceneFactory.provideBritrockScene(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 14: // @javax.inject.Named("scene_bano_bar_lima") com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer
          return (T) OrganicScenesModule_ProvideBanoBarLimaSceneFactory.provideBanoBarLimaScene(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 15: // @javax.inject.Named("scene_woodstock") com.jfcardenas.musicwall.features.wallpaper.renderer.organic.OrganicRenderer
          return (T) OrganicScenesModule_ProvideWoodstockSceneFactory.provideWoodstockScene(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 16: // com.jfcardenas.musicwall.api.DeezerService
          return (T) NetworkModule_ProvideDeezerServiceFactory.provideDeezerService();

          case 17: // com.jfcardenas.musicwall.data.CoverFallbackRepository
          return (T) new CoverFallbackRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideDeezerServiceProvider.get(), singletonCImpl.albumDao(), singletonCImpl.coverUpdateBusProvider.get());

          case 18: // com.jfcardenas.musicwall.data.CoverUpdateBus
          return (T) new CoverUpdateBus();

          case 19: // com.jfcardenas.musicwall.scrobble.ScrobbleRepository
          return (T) new ScrobbleRepository(singletonCImpl.localScrobbleDao(), singletonCImpl.lastFmTrackResolverProvider.get());

          case 20: // com.jfcardenas.musicwall.scrobble.LastFmTrackResolver
          return (T) new LastFmTrackResolver(singletonCImpl.provideLastFmServiceProvider.get(), singletonCImpl.lfMatchCacheDao());

          case 21: // com.jfcardenas.musicwall.scrobble.NowPlayingBus
          return (T) new NowPlayingBus();

          case 22: // com.jfcardenas.musicwall.api.LyricsService
          return (T) NetworkModule_ProvideLyricsServiceFactory.provideLyricsService();

          case 23: // com.jfcardenas.musicwall.api.LrcLibService
          return (T) NetworkModule_ProvideLrcLibServiceFactory.provideLrcLibService();

          case 24: // com.jfcardenas.musicwall.api.DiscogsService
          return (T) NetworkModule_ProvideDiscogsServiceFactory.provideDiscogsService();

          case 25: // com.jfcardenas.musicwall.api.SpotifyOEmbedService
          return (T) SpotifyModule_ProvideSpotifyOEmbedServiceFactory.provideSpotifyOEmbedService();

          case 26: // com.jfcardenas.musicwall.scrobble.NowPlayingReader
          return (T) new NowPlayingReader(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 27: // com.jfcardenas.musicwall.scrobble.ScrobbleEngine
          return (T) new ScrobbleEngine(singletonCImpl.scrobbleRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
