package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

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
}
