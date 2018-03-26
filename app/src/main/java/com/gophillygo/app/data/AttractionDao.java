package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.gophillygo.app.data.models.Attraction;

import java.util.List;


/**
 * Database access methods shared by destinations and events.
 */

interface AttractionDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void save(T obj);

    @Update()
    abstract void update(T obj);

    @Delete()
    abstract  void delete(T obj);

    abstract void clear();

    @Transaction
    void bulkUpdate(List<T> objs);
}
