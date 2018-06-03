package com.gophillygo.app.di;

import com.gophillygo.app.tasks.AddGeofencesBroadcastReceiver;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AddGeofenceBroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract AddGeofencesBroadcastReceiver contributeAddGeofenceBroadcastReceiver();
}
