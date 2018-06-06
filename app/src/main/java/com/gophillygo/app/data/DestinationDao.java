package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.gophillygo.app.data.models.AttractionFlag;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;

import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public abstract class DestinationDao implements AttractionDao<Destination> {
    @Query("SELECT destination.*, COUNT(event.id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attractionID AND attractionflag.is_event = 0 " +
            "GROUP BY destination.id " +
            "ORDER BY distance ASC")
    public abstract LiveData<List<DestinationInfo>> getAll();

    @Query("SELECT destination.*, COUNT(event.id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attractionID AND attractionflag.is_event = 0 " +
            "WHERE destination.id = :destinationId " +
            "GROUP BY destination.id")
    abstract LiveData<DestinationInfo> getDestination(long destinationId);

    @Query("DELETE FROM destination")
    public abstract void clear();

    @Transaction
    public void bulkUpdate(List<Destination> destinations) {
        for (Destination destination: destinations) {
            update(destination);
        }
    }

    /**
     * Find those destinations with a given flag set, which are those that should be geofenced.
     *
     * Must be accessed on a background thread.
     *
     * @return Destination objects, without related event info
     */

    @Query(value = "SELECT destination.* FROM destination INNER JOIN attractionflag " +
            "ON destination.id = attractionflag.attractionID AND attractionflag.is_event = 0 " +
            "WHERE attractionflag.option = :geofenceFlagCode")
    public abstract List<Destination> getGeofenceDestinations(int geofenceFlagCode);

    /**
     * Get a single destination.
     *
     * Must be accessed from a background thread.
     *
     * @param destinationId ID of place to fetch
     * @return Matching destination, with related event count and user flag.
     */
    @Query("SELECT * FROM destination WHERE destination.id = :destinationId")
    public abstract Destination getDestinationInBackground(long destinationId);
}
