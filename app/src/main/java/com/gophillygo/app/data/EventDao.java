package com.gophillygo.app.data;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.gophillygo.app.data.models.Event;

import java.util.List;

/**
 * Database access methods for events.
 */

@Dao
public abstract class EventDao {
    @Query("SELECT * FROM event")
    abstract LiveData<List<Event>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void save(Event event);

    @Query("SELECT * FROM event WHERE id = :eventId")
    abstract LiveData<Event> getEvent(long eventId);

    @Query("DELETE FROM event")
    abstract void clear();

    @Update()
    abstract void update(Event event);

    @Transaction
    void bulkUpdate(List<Event> events) {
        for (Event event: events) {
            update(event);
        }
    }
}
