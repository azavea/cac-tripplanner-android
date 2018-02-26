package com.gophillygo.app.di;

import com.gophillygo.app.PlacesListActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PlacesListActivityModule {
    @SuppressWarnings("unused")
    @ContributesAndroidInjector
    abstract PlacesListActivity contributePlacesListActivity();
}
