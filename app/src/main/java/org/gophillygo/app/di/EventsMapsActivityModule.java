package org.gophillygo.app.di;

import org.gophillygo.app.activities.EventsMapsActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract public class EventsMapsActivityModule {
    @ContributesAndroidInjector
    abstract EventsMapsActivity contributeEventsMapsActivity();
}
