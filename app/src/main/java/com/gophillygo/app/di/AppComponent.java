package com.gophillygo.app.di;

import android.app.Application;

import com.gophillygo.app.GoPhillyGoApp;
import com.gophillygo.app.PlacesListActivity;

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
        HomeActivityModule.class,
        PlacesListActivityModule.class
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
