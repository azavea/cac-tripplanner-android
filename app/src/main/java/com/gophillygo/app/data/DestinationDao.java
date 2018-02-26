package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.gophillygo.app.data.models.Destination;

import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public abstract class DestinationDao {
    @Query("SELECT * FROM destination ORDER BY distance ASC")
    abstract LiveData<List<Destination>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void save(Destination destination);

    @Query("SELECT * FROM destination WHERE id = :destinationId")
    abstract LiveData<Destination> getDestination(String destinationId);

    @Query("DELETE FROM destination")
    abstract void clear();

    @Update()
    abstract void update(Destination destination);

    @Transaction
    void bulkUpdate(List<Destination> destinations) {
        for (Destination destination: destinations) {
            update(destination);
        }
    }
}
