package org.gophillygo.app.data;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;


/**
 * Database access methods shared by destinations and events.
 */

interface AttractionDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(T obj);

    @Update()
    void update(T obj);

    @Delete()
    void delete(T obj);

    @SuppressWarnings({"EmptyMethod", "unused"})
    void clear();

    @Transaction
    void bulkUpdate(List<T> objs);
}
