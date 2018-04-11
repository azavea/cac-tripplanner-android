package com.gophillygo.app.data;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.gophillygo.app.data.models.AttractionFlag;

import java.util.List;

/**
 * Database access methods for user flags on events and destinations.
 */

@Dao
public abstract class AttractionFlagDao {
    @Query("SELECT * FROM attractionflag WHERE is_event = :isEvent;")
    public abstract LiveData<List<AttractionFlag>> getAttractionFlags(boolean isEvent);

    @Query("SELECT * FROM attractionflag " +
            "WHERE attractionID = :attractionID and is_event = :isEvent")
    public abstract LiveData<AttractionFlag> getAttractionFlag(long attractionID, boolean isEvent);

    @Update()
    public abstract void update(AttractionFlag flag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(AttractionFlag flag);
}
