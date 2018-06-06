package com.gophillygo.app.di;

import com.gophillygo.app.tasks.GeofenceTransitionBroadcastReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class GeofenceTransitionBroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract GeofenceTransitionBroadcastReceiver contributeGeofenceTransitionBroadcastReceiver();
}
