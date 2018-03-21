package com.gophillygo.app.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.gophillygo.app.data.models.Destination;

@Database(version=4, entities={Destination.class})
@TypeConverters({RoomConverters.class})
public abstract class GpgDatabase extends RoomDatabase {
    abstract public DestinationDao destinationDao();
}
