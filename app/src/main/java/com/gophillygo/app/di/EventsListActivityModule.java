package com.gophillygo.app.di;

import com.gophillygo.app.activities.EventsListActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class EventsListActivityModule {
    @ContributesAndroidInjector
    abstract EventsListActivity contributeEventsListActivity();
}
