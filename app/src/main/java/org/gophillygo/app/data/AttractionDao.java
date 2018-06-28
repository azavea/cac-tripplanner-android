package org.gophillygo.app.data;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import android.database.Cursor;

import java.util.List;


/**
 * Database access methods shared by destinations and events.
 */

interface AttractionDao<T> {

    @Query("SELECT destination._id, destination.name AS suggest_text_1, " +
            "'android.resource://org.gophillygo.app/2131165333' AS suggest_icon_1 " +
            "FROM destination " +
            "WHERE destination.name LIKE :search " +
            "UNION " +
            "SELECT event._id, event.name AS suggest_text_1, " +
            "'android.resource://org.gophillygo.app/2131165314' AS suggest_icon_1 " +
            "FROM event " +
            "WHERE event.name LIKE :search ")
    Cursor searchAttractions(String search);

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
