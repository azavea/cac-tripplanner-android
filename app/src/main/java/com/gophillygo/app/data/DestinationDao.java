package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.gophillygo.app.data.models.Destination;

import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public interface DestinationDao {
    @Query("SELECT * FROM destination")
    LiveData<List<Destination>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(Destination destination);

    @Query("SELECT * FROM destination WHERE id = :destinationId")
    LiveData<Destination> getDestination(String destinationId);

    @Query("DELETE FROM destination")
    void clear();
}
