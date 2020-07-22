package org.gophillygo.app;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.gophillygo.app.di.AppInjector;
import org.gophillygo.app.utils.UserUtils;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.DaggerApplication;

/**
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/178fe541643adb122d2a8925cf61a21950a4611c/GithubBrowserSample/app/src/main/java/com/android/example/github/GithubApp.java
 */


public class GoPhillyGoApp extends Application implements HasAndroidInjector {



    private static final String LOG_LABEL = "GPGApp";

    @SuppressWarnings("WeakerAccess")
    @Inject
    DispatchingAndroidInjector<Object> dispatchingInjector;

    private volatile boolean needToInject = true;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.d(LOG_LABEL, "Running in debug mode");
        }


        // Lazy initialization to support injection for content provider. See:
        // https://github.com/google/dagger/blob/master/java/dagger/android/DaggerApplication.java
        injectIfNecessary();

        // Initialize Firebase Crashlytics crash and usage data logging.
        // Disable if user setting turned off
        boolean enableAnalytics = UserUtils.isCrashlyticsEnabled(this);

        if (enableAnalytics) {
            Log.d(LOG_LABEL, "++++++++++++++++++++++++++++++++++++++++++++");
            Log.d(LOG_LABEL, "Crashlytics reporting is enabled");
            Log.d(LOG_LABEL, "++++++++++++++++++++++++++++++++++++++++++++");
        } else {
            Log.d(LOG_LABEL, "----------------------------------------------");
            Log.d(LOG_LABEL, "Crashlytics reporting is disabled");
            Log.d(LOG_LABEL, "----------------------------------------------");
        }

        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enableAnalytics);
    }

    /**
     * Lazily injects the {@link DaggerApplication}'s members. Injection cannot be performed in {@link
     * Application#onCreate()} since {@link android.content.ContentProvider}s' {@link
     * android.content.ContentProvider#onCreate() onCreate()} method will be called first and might
     * need injected members on the application. Injection is not performed in the constructor, as
     * that may result in members-injection methods being called before the constructor has completed,
     * allowing for a partially-constructed instance to escape.
     */
    private void injectIfNecessary() {
        if (needToInject) {
            synchronized (this) {
                if (needToInject) {
                    AppInjector.init(this);
                    if (needToInject) {
                        throw new IllegalStateException(
                                "The AndroidInjector returned from applicationInjector() did not inject the "
                                        + "DaggerApplication");
                    }
                }
            }
        }
    }

    @Inject
    void setInjected() {
        needToInject = false;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        injectIfNecessary();
        return dispatchingInjector;
    }
}