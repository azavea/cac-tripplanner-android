package com.gophillygo.app.data;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.gophillygo.app.data.models.AttractionFlag;

/**
 * Database access methods for user flags on events and destinations.
 */

@Dao
public abstract class AttractionFlagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(AttractionFlag flag);
}
