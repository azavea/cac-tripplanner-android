package com.gophillygo.app.di;

import com.gophillygo.app.activities.PlacesListActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PlacesListActivityModule {
    @ContributesAndroidInjector
    abstract PlacesListActivity contributePlacesListActivity();
}
