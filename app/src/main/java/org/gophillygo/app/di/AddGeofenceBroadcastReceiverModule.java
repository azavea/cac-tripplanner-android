package org.gophillygo.app.di;

import org.gophillygo.app.tasks.AddGeofencesBroadcastReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AddGeofenceBroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract AddGeofencesBroadcastReceiver contributeAddGeofenceBroadcastReceiver();
}
