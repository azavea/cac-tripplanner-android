package com.gophillygo.app.di;

import com.gophillygo.app.activities.MapsActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract public class MapsActivityModule {
    @ContributesAndroidInjector
    abstract MapsActivity contributeMapsActivity();
}
