package com.gophillygo.app.di;

import com.gophillygo.app.activities.EventDetailActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class EventDetailActivityModule {
    @ContributesAndroidInjector
    abstract EventDetailActivity contributeEventDetailActivity();
}
