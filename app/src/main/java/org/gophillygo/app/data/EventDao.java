package org.gophillygo.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.gophillygo.app.data.models.Event;
import org.gophillygo.app.data.models.EventInfo;

import java.util.List;

/**
 * Database access methods for events.
 */

@Dao
public abstract class EventDao implements AttractionDao<Event> {
    @Transaction
    @Query("SELECT event.*, destination.name AS destinationName, " +
            "destination.categories AS destinationCategories, attractionflag.option, " +
            "destination.distance AS distance, destination.x, destination.y, destination.watershed_alliance " +
            "FROM event " +
            "LEFT JOIN destination ON destination._id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON event._id = attractionflag.attraction_id AND attractionflag.is_event = 1 " +
            "ORDER BY event.start_date ASC;")
    public abstract LiveData<List<EventInfo>> getAll();

    @Transaction
    @Query("SELECT event.*, destination.name AS destinationName, NULL AS distance, " +
            "destination.categories AS destinationCategories, attractionflag.option, " +
            "destination.distance, destination.x, destination.y, destination.watershed_alliance " +
            "FROM event " +
            "LEFT JOIN destination ON destination._id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON event._id = attractionflag.attraction_id AND attractionflag.is_event = 1 " +
            "WHERE destination._id = :destinationId " +
            "ORDER BY event.start_date ASC;")
    public abstract LiveData<List<EventInfo>> getEventsForDestination(long destinationId);

    @Query("SELECT event.*, destination.name AS destinationName, " +
            "destination.categories AS destinationCategories, attractionflag.option, " +
            "destination.distance AS distance, destination.x, destination.y, destination.watershed_alliance " +
            "FROM event " +
            "LEFT JOIN destination ON destination._id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON event._id = attractionflag.attraction_id AND attractionflag.is_event = 1 " +
            "WHERE event._id = :eventId")
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
