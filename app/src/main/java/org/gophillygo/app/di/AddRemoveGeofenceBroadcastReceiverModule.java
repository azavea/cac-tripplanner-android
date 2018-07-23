package org.gophillygo.app.di;

import org.gophillygo.app.tasks.AddRemoveGeofencesBroadcastReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AddRemoveGeofenceBroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract AddRemoveGeofencesBroadcastReceiver contributeAddRemoveGeofenceBroadcastReceiver();
}
