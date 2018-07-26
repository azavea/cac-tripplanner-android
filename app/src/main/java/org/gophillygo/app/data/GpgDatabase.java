package org.gophillygo.app.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.Event;

@Database(version=14, entities={AttractionFlag.class, Destination.class, Event.class})
@TypeConverters({RoomConverters.class})
public abstract class GpgDatabase extends RoomDatabase {
    abstract public DestinationDao destinationDao();
    abstract public EventDao eventDao();
    abstract public AttractionFlagDao attractionFlagDao();
}
