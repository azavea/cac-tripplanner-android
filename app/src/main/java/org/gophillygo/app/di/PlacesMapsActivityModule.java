package org.gophillygo.app.di;

import org.gophillygo.app.activities.PlacesMapsActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract public class PlacesMapsActivityModule {
    @ContributesAndroidInjector
    abstract PlacesMapsActivity contributePlacesMapsActivity();
}
