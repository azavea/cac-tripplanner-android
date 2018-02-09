package com.gophillygo.app.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public interface DestinationDao {
    @Query("SELECT * FROM destination")
    List<Destination> getAll();
}
