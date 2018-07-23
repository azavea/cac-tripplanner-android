package org.gophillygo.app.di;

import org.gophillygo.app.activities.GpgPreferenceActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class GpgPreferenceActivityModule {
    @ContributesAndroidInjector
    abstract GpgPreferenceActivity contributeGpgPreferenceActivity();
}
