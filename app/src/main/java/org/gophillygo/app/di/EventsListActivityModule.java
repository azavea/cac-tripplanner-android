package org.gophillygo.app.di;

import org.gophillygo.app.activities.EventsListActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class EventsListActivityModule {
    @ContributesAndroidInjector
    abstract EventsListActivity contributeEventsListActivity();
}
