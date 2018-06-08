package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.support.annotation.Nullable;

import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.CategoryAttraction;
import com.gophillygo.app.data.models.CategoryImage;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public abstract class DestinationDao implements AttractionDao<Destination> {

    public static final int NUM_DESTINATION_CATEGORIES = 5;

    @Transaction
    @Query("SELECT destination.*, COUNT(event.id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
            "GROUP BY destination.id " +
            "ORDER BY distance ASC")
    public abstract LiveData<List<DestinationInfo>> getAll();

    @Query("SELECT destination.*, COUNT(event.id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination.id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
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

    @Query("SELECT id AS attractionID, 1 AS is_event, image FROM event ORDER BY random() LIMIT 1")
    protected abstract LiveData<CategoryImage> getEventCategoryImage();

    /**
     * Find random representative images for each of the filter buttons on the home screen.
     * Must be called from background thread.
     *
     * One category is events, fetched here instead of {@link EventDao} for convenience and to
     * keep requests to a single transaction. The others are based on either destination category
     * or user flag.
     *
     * Attempts to find a unique, randomized set of {@link Attraction} for each filter button,
     * but may return duplicates.
     *
     * @return One randomized image for each of the home page grid of filter categories
     */
    @Transaction
    public LiveData<List<CategoryAttraction>> getCategoryImages() {
        ArrayList<CategoryAttraction> categoryAttractions = new ArrayList<>(NUM_DESTINATION_CATEGORIES + 1);
        // one query for event

        // TODO: 5 queries for destinations: ORDER BY RANDOM() LIMIT 5
        // TODO: order queries by result size, ascending
        // TODO: pick first from each result set that hasn't already been used, or reuse last


        MediatorLiveData<List<CategoryAttraction>> data = new MediatorLiveData<>();
        LiveData<CategoryImage> eventCategory = getEventCategoryImage();
        data.addSource(eventCategory, categoryImage -> {
            if (categoryImage == null) return;
            CategoryAttraction attraction = new CategoryAttraction(CategoryAttraction.PlaceCategories.Events.code, categoryImage.getImage());
            categoryAttractions.add(attraction);

            // FIXME: remove
            String image = categoryImage.getImage();
            CategoryAttraction one = new CategoryAttraction(CategoryAttraction.PlaceCategories.WantToGo.code, image);
            CategoryAttraction two = new CategoryAttraction(CategoryAttraction.PlaceCategories.Liked.code, image);
            CategoryAttraction three = new CategoryAttraction(CategoryAttraction.PlaceCategories.Nature.code, image);
            CategoryAttraction four = new CategoryAttraction(CategoryAttraction.PlaceCategories.Exercise.code, image);
            CategoryAttraction five = new CategoryAttraction(CategoryAttraction.PlaceCategories.Educational.code, image);

            categoryAttractions.add(one);
            categoryAttractions.add(two);
            categoryAttractions.add(three);
            categoryAttractions.add(four);
            categoryAttractions.add(five);
            ///////////////////////////////////////

            data.postValue(categoryAttractions);
            data.removeSource(eventCategory);
        });

        return data;
    }

    /**
     *
     * @return One random attraction per category
     */
    /* FIXME:
    @Query("SELECT attractionflag.option, destination.image " +
            "FROM destination " +
            "LEFT JOIN attractionflag " +
            "ON destination.id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
            "GROUP BY attractionflag.option")
    public abstract LiveData<String> getRandomLikedWantToGoImages();
    */

    /**
     * Find those destinations with a given flag set, which are those that should be geofenced.
     *
     * Must be accessed on a background thread.
     *
     * @return Destination objects, without related event info
     */

    @Query(value = "SELECT destination.* FROM destination INNER JOIN attractionflag " +
            "ON destination.id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
            "WHERE attractionflag.option = :geofenceFlagCode")
    public abstract List<Destination> getGeofenceDestinations(int geofenceFlagCode);

    /**
     * Get a single destination.
     *
     * Must be accessed from a background thread.
     *
     * @param destinationId ID of place to fetch
     * @return Matching destination, with related event count and user flag.
     */
    @Query("SELECT * FROM destination WHERE destination.id = :destinationId")
    public abstract Destination getDestinationInBackground(long destinationId);
}
