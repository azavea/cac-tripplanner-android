package com.gophillygo.app.data;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.gophillygo.app.data.models.Event;

import java.util.List;

/**
 * Database access methods for events.
 */

@Dao
public abstract class EventDao implements AttractionDao<Event> {
    @Query("SELECT event.*, destination.name AS destinationName FROM event " +
            "LEFT JOIN destination ON destination.id = event.destination " +
            "ORDER BY event.start_date ASC;")
    public abstract LiveData<List<Event>> getAll();

    @Query("SELECT * FROM event WHERE id = :eventId")
    public abstract LiveData<Event> getEvent(long eventId);

    @Query("SELECT event.*, destination.name AS destinationName FROM event " +
            "INNER JOIN destination ON destination.id = event.destination " +
            "WHERE event.destination = :destinationId " +
            "ORDER BY event.start_date ASC;")
    public abstract LiveData<List<Event>> getEventsForDestination(long destinationId);

    @Query("DELETE FROM event")
    public abstract void clear();

    @Transaction
    public void bulkUpdate(List<Event> events) {
        for (Event event: events) {
            update(event);
        }
    }
}
