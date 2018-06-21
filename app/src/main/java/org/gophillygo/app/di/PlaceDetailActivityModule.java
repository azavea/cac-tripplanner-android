package org.gophillygo.app.di;

import org.gophillygo.app.activities.PlaceDetailActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PlaceDetailActivityModule {
    @ContributesAndroidInjector
    abstract PlaceDetailActivity contributePlaceDetailActivity();
}
