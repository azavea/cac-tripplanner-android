package org.gophillygo.app.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import org.gophillygo.app.data.models.AttractionFlag;

/**
 * Database access methods for user flags on events and destinations.
 */

@Dao
public abstract class AttractionFlagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void save(AttractionFlag flag);
}
