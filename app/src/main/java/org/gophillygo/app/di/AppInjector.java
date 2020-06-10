package org.gophillygo.app.di;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.gophillygo.app.GoPhillyGoApp;

import dagger.android.AndroidInjection;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.HasAndroidInjector;


/**
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/178fe541643adb122d2a8925cf61a21950a4611c/GithubBrowserSample/app/src/main/java/com/android/example/github/di/AppInjector.java
 */

public class AppInjector {
    private AppInjector() {}
    public static void init(GoPhillyGoApp app) {
        DaggerAppComponent.builder().application(app)
                .build().inject(app);
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                handleActivity(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

    private static void handleActivity(Activity activity) {
        if (activity instanceof HasAndroidInjector) {
            AndroidInjection.inject(activity);
        }
        if (activity instanceof FragmentActivity) {
            ((FragmentActivity) activity).getSupportFragmentManager()
                    .registerFragmentLifecycleCallbacks(
                            new FragmentManager.FragmentLifecycleCallbacks() {
                                @Override
                                public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f,
                                                              Bundle savedInstanceState) {
                                    if (f instanceof Injectable) {
                                        AndroidSupportInjection.inject(f);
                                    }
                                }
                            }, true);
        }

        if (activity instanceof AppCompatActivity) {
            AndroidInjection.inject(activity);
        }
    }
}
