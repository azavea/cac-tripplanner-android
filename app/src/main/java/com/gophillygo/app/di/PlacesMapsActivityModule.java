package com.gophillygo.app.di;

import com.gophillygo.app.activities.PlacesMapsActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract public class PlacesMapsActivityModule {
    @ContributesAndroidInjector
    abstract PlacesMapsActivity contributePlacesMapsActivity();
}
