package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.gophillygo.app.data.models.Destination;

import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public abstract class DestinationDao implements AttractionDao<Destination> {
    @Query("SELECT * FROM destination ORDER BY distance ASC")
    public abstract LiveData<List<Destination>> getAll();


    @Query("SELECT * FROM destination WHERE id = :destinationId")
    abstract LiveData<Destination> getDestination(long destinationId);

    @Query("DELETE FROM destination")
    public abstract void clear();

    @Transaction
    public void bulkUpdate(List<Destination> destinations) {
        for (Destination destination: destinations) {
            update(destination);
        }
    }
}
