package com.gophillygo.app.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.gophillygo.app.data.models.Destination;

@Database(version=1, entities={Destination.class})
public abstract class GpgDatabase extends RoomDatabase {
    abstract public DestinationDao destinationDao();
}
