package com.gophillygo.app.data;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.models.EventInfo;

import java.util.List;

/**
 * Database access methods for events.
 */

@Dao
public abstract class EventDao implements AttractionDao<Event> {
    @Query("SELECT event.*, destination.name AS destinationName, NULL AS distance, " +
            "destination.categories AS destinationCategories, attractionflag.option, " +
            "destination.x, destination.y " +
            "FROM event " +
            "LEFT JOIN destination ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON event.id = attractionflag.attractionID AND attractionflag.is_event = 1 " +
            "ORDER BY event.start_date ASC;")
    public abstract LiveData<List<EventInfo>> getAll();

    @Query("SELECT event.*, destination.name AS destinationName, destination.distance AS distance, " +
            "destination.categories AS destinationCategories, attractionflag.option, " +
            "destination.x, destination.y " +
            "FROM event " +
            "LEFT JOIN destination ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON event.id = attractionflag.attractionID AND attractionflag.is_event = 1 " +
            "WHERE event.id = :eventId")
    public abstract LiveData<EventInfo> getEvent(long eventId);

    @Query("DELETE FROM event")
    public abstract void clear();

    @Transaction
    public void bulkUpdate(List<Event> events) {
        for (Event event: events) {
            update(event);
        }
    }
}
