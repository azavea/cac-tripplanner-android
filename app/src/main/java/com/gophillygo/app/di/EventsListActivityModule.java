package com.gophillygo.app.di;

import com.gophillygo.app.EventsListActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class EventsListActivityModule {
    @SuppressWarnings("unused")
    @ContributesAndroidInjector
    abstract EventsListActivity contributeEventsListActivity();
}
