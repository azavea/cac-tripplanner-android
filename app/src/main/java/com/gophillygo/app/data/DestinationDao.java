package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.gophillygo.app.data.models.AttractionFlag;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public abstract class DestinationDao implements AttractionDao<Destination> {
    @Query("SELECT destination.*, COUNT(event.id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attractionID AND attractionflag.is_event = 0 " +
            "GROUP BY destination.id " +
            "ORDER BY distance ASC")
    public abstract LiveData<List<DestinationInfo>> getAll();

    public LiveData<List<DestinationInfo>> getFiltered(Filter filter) {
        List<String> categories = new ArrayList<>();
        if (filter.nature) {
            categories.add("Nature");
        }
        if (filter.exercise) {
            categories.add("Exercise");
        }
        if (filter.educational) {
            categories.add("Educational");
        }

        List<Integer> flags = new ArrayList<>();
        if (filter.been) {
            flags.add(AttractionFlag.Option.Been.code);
        }
        if (filter.liked) {
            flags.add(AttractionFlag.Option.Liked.code);
        }
        if (filter.notInterested) {
            flags.add(AttractionFlag.Option.NotInterested.code);
        }
        if (filter.wantToGo) {
            flags.add(AttractionFlag.Option.WantToGo.code);
        }

        return getFiltered(!categories.isEmpty(), categories, !flags.isEmpty(), flags, filter.accessible);
    }

    @Query("SELECT destination.*, COUNT(event.id) AS eventCount, attractionflag.option " +
            "FROM destination "+
            "LEFT JOIN event ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attractionID AND attractionflag.is_event = 0 " +
            "WHERE " +"" +
            " CASE :searchCategories " +
            " WHEN 1 THEN destination.categories IN (:categories) " +
            " ELSE 1 END " +
            "AND CASE :searchFlags " +
            " WHEN 1 THEN attractionflag.option IN (:flags) " +
            " ELSE 1 END " +
            "AND CASE :accessible " +
            " WHEN 1 THEN destination.accessible = 1 " +
            " ELSE 1 END " +
            "GROUP BY destination.id " +
            "ORDER BY distance ASC")
    protected abstract LiveData<List<DestinationInfo>> getFiltered(boolean searchCategories, List<String> categories,
                                                                   boolean searchFlags, List<Integer> flags,
                                                                   boolean accessible);

    @Query("SELECT destination.*, COUNT(event.id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attractionID AND attractionflag.is_event = 0 " +
            "WHERE destination.id = :destinationId " +
            "GROUP BY destination.id")
    abstract LiveData<DestinationInfo> getDestination(long destinationId);

    @Query("DELETE FROM destination")
    public abstract void clear();

    @Transaction
    public void bulkUpdate(List<Destination> destinations) {
        for (Destination destination: destinations) {
            update(destination);
        }
    }
}
