package org.gophillygo.app.di;

import org.gophillygo.app.tasks.GeofenceTransitionBroadcastReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class GeofenceTransitionBroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract GeofenceTransitionBroadcastReceiver contributeGeofenceTransitionBroadcastReceiver();
}
