package org.gophillygo.app.di;

import org.gophillygo.app.activities.AppInfoActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AppInfoActivityModule {
    @ContributesAndroidInjector
    abstract AppInfoActivity  contributeAppInfoActivity();
}
