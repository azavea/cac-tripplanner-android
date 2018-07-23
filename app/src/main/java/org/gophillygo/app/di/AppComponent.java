package org.gophillygo.app.di;

import android.app.Application;

import org.gophillygo.app.GoPhillyGoApp;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/e33782ba54ebe87f7e21e03542230695bc893818/GithubBrowserSample/app/src/main/java/com/android/example/github/di/AppComponent.java
 */

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AppModule.class,

        // Activities
        EventDetailActivityModule.class,
        EventsListActivityModule.class,
        HomeActivityModule.class,
        PlaceDetailActivityModule.class,
        PlacesListActivityModule.class,
        EventsMapsActivityModule.class,
        PlacesMapsActivityModule.class,
        GpgPreferenceActivityModule.class,
        OssLicensesMenuActivityModule.class,
        OssLicensesActivityModule.class,
        SearchActivityModule.class,
        AppInfoActivityModule.class,

        // Broadcast Receivers
        AddRemoveGeofenceBroadcastReceiverModule.class,
        GeofenceTransitionBroadcastReceiverModule.class,

        // Content Providers
        GoPhillyGoContentProviderModule.class
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        AppComponent build();
    }
    void inject(GoPhillyGoApp goPhillyGoApp);
}
