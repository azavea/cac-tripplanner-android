package org.gophillygo.app.di;

import org.gophillygo.app.GoPhillyGoContentProvider;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class GoPhillyGoContentProviderModule {

    @ContributesAndroidInjector
    abstract GoPhillyGoContentProvider contributeGoPhillyGoContentProvider();
}
