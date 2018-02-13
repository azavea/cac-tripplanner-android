package com.gophillygo.app;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.gophillygo.app.di.AppInjector;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

/**
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/178fe541643adb122d2a8925cf61a21950a4611c/GithubBrowserSample/app/src/main/java/com/android/example/github/GithubApp.java
 */


public class GoPhillyGoApp extends Application implements HasActivityInjector {

    private static final String LOG_LABEL = "GPGApp";

    @SuppressWarnings("WeakerAccess")
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Log.d(LOG_LABEL, "Running in debug mode");
        }
        AppInjector.init(this);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}