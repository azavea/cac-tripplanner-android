package org.gophillygo.app.di;

import org.gophillygo.app.activities.SearchActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract public class SearchActivityModule {
    @ContributesAndroidInjector
    abstract SearchActivity contributeSearchActivity();
}
