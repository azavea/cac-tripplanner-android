package org.gophillygo.app.data;

import android.database.Cursor;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;


/**
 * Database access methods shared by destinations and events.
 */

interface AttractionDao<T> {

    @Query("SELECT destination._id AS _id, destination.name AS suggest_text_1, " +
            "'android.resource://org.gophillygo.app/2131165334' AS suggest_icon_1, " +
            "0 AS suggest_intent_data " +
            "FROM destination " +
            "WHERE destination.name LIKE :search " +
            "UNION " +
            "SELECT event._id AS _id, event.name AS suggest_text_1, " +
            "'android.resource://org.gophillygo.app/2131165314' AS suggest_icon_1, " +
            "1 AS suggest_intent_data " +
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
