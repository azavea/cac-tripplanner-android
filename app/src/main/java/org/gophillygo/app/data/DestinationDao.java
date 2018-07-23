package org.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.util.Log;

import org.gophillygo.app.data.models.Attraction;
import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.CategoryAttraction;
import org.gophillygo.app.data.models.CategoryImage;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.DestinationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Database access methods for destinations.
 */

@Dao
public abstract class DestinationDao implements AttractionDao<Destination> {

    private static final String LOG_LABEL = "DestinationDao";

    @Transaction
    @Query("SELECT destination.*, COUNT(event._id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination._id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination._id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
            "GROUP BY destination._id " +
            "ORDER BY distance ASC")
    public abstract LiveData<List<DestinationInfo>> getAll();

    @Query("SELECT destination.*, COUNT(event._id) AS eventCount, attractionflag.option " +
            "FROM destination " +
            "LEFT JOIN event ON destination._id = event.destination " +
            "LEFT JOIN attractionflag " +
            "ON destination._id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
            "WHERE destination._id = :destinationId " +
            "GROUP BY destination._id")
    abstract LiveData<DestinationInfo> getDestination(long destinationId);

    @Query("DELETE FROM destination")
    public abstract void clear();

    @Transaction
    public void bulkUpdate(List<Destination> destinations) {
        for (Destination destination: destinations) {
            update(destination);
        }
    }

    @Query("SELECT _id AS attractionID, 1 AS is_event, wide_image AS image FROM event ORDER BY random() LIMIT 1")
    protected abstract LiveData<CategoryImage> getEventCategoryImage();

    @Query("SELECT _id AS attractionID, 0 AS is_event, destination.wide_image AS image " +
            "FROM destination " +
            "LEFT JOIN attractionflag " +
            "ON destination._id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
            "WHERE attractionflag.option = :flag " +
            "ORDER BY random() " +
            "LIMIT 1")
    public abstract LiveData<CategoryImage> getRandomImagesForFlag(int flag);

    @Query("SELECT _id AS attractionID, 0 AS is_event, destination.wide_image AS image " +
            "FROM destination " +
            "WHERE nature = 1 " +
            "ORDER BY random() " +
            "LIMIT 1")
    public abstract LiveData<CategoryImage> getRandomNatureImages();

    @Query("SELECT _id AS attractionID, 0 AS is_event, destination.wide_image AS image " +
            "FROM destination " +
            "WHERE exercise = 1 " +
            "ORDER BY random() " +
            "LIMIT 1")
    public abstract LiveData<CategoryImage> getRandomExerciseImages();

    @Query("SELECT _id AS attractionID, 0 AS is_event, destination.wide_image AS image " +
            "FROM destination " +
            "WHERE watershed_alliance = 1 " +
            "ORDER BY random() " +
            "LIMIT 1")
    public abstract LiveData<CategoryImage> getRandomWatershedAllianceImages();

    /**
     * Find random representative images for each of the filter buttons on the home screen.
     * Must be called from background thread.
     *
     * One category is events, fetched here instead of {@link EventDao} for convenience and to
     * keep requests to a single transaction. The others are based on either destination category
     * or user flag.
     *
     * Gets a randomized set of {@link Attraction} for each filter button. May return duplicates.
     *
     * @return One randomized image for each of the home page grid of filter categories
     */
    @Transaction
    public LiveData<List<CategoryAttraction>> getCategoryImages() {
        ArrayList<CategoryAttraction> categoryAttractions = new ArrayList<>(CategoryAttraction.PlaceCategories.size());
        for (CategoryAttraction.PlaceCategories placeCategory : CategoryAttraction.PlaceCategories.values()) {
            categoryAttractions.add(placeCategory.code, new CategoryAttraction(placeCategory.code, ""));
        }
        Log.d(LOG_LABEL, "created category grid adapter");

        MediatorLiveData<List<CategoryAttraction>> data = new MediatorLiveData<>();
        LiveData<CategoryImage> event = getEventCategoryImage();
        addSourceToRandomImagesLiveData(event, CategoryAttraction.PlaceCategories.Events, categoryAttractions, data);

        LiveData<CategoryImage> wantToGo = getRandomImagesForFlag(AttractionFlag.Option.WantToGo.code);
        addSourceToRandomImagesLiveData(wantToGo, CategoryAttraction.PlaceCategories.WantToGo, categoryAttractions, data);

        LiveData<CategoryImage> liked = getRandomImagesForFlag(AttractionFlag.Option.Liked.code);
        addSourceToRandomImagesLiveData(liked, CategoryAttraction.PlaceCategories.Liked, categoryAttractions, data);

        LiveData<CategoryImage> watershedAlliance = getRandomWatershedAllianceImages();
        addSourceToRandomImagesLiveData(watershedAlliance, CategoryAttraction.PlaceCategories.WatershedAlliance,
                categoryAttractions, data);

        LiveData<CategoryImage> nature = getRandomNatureImages();
        addSourceToRandomImagesLiveData(nature, CategoryAttraction.PlaceCategories.Nature, categoryAttractions, data);

        LiveData<CategoryImage> exercise = getRandomExerciseImages();
        addSourceToRandomImagesLiveData(exercise, CategoryAttraction.PlaceCategories.Exercise, categoryAttractions, data);

        LiveData<CategoryImage> educational = getRandomExerciseImages();
        addSourceToRandomImagesLiveData(educational, CategoryAttraction.PlaceCategories.Educational, categoryAttractions, data);
        return data;
    }

    /**
     * Helper method to build a synthesized `LiveData` object containing all categories
     * for the home page grid of random images.
     *
     * @param source A `LiveData` result to add to the set of `LiveData`
     * @param category Which category this is for
     * @param categoryAttractions Set of results being built
     * @param data The `MediatorLiveData` to attach this source to
     */
    private void addSourceToRandomImagesLiveData(LiveData<CategoryImage> source,
                                                 CategoryAttraction.PlaceCategories category,
                                                 ArrayList<CategoryAttraction> categoryAttractions,
                                                 MediatorLiveData<List<CategoryAttraction>> data) {

        data.addSource(source, categoryImage -> {
            if (categoryImage == null) {
                return;
            }
            CategoryAttraction attraction = new CategoryAttraction(category.code, categoryImage.getImage());
            categoryAttractions.set(category.code, attraction);
            data.postValue(categoryAttractions);
            data.removeSource(source);
        });
    }

    /**
     * Find those destinations with a given flag set, which are those that should be geofenced.
     *
     * Must be accessed on a background thread.
     *
     * @return Destination objects, without related event info
     */

    @Query(value = "SELECT destination.* FROM destination INNER JOIN attractionflag " +
            "ON destination._id = attractionflag.attraction_id AND attractionflag.is_event = 0 " +
            "WHERE attractionflag.option = :wantToGoCode OR attractionflag.option = :likedCode")
    public abstract List<Destination> getGeofenceDestinations(int wantToGoCode, int likedCode);

    /**
     * Get a single destination.
     *
     * Must be accessed from a background thread.
     *
     * @param destinationId ID of place to fetch
     * @return Matching destination, with related event count and user flag.
     */
    @Query("SELECT * FROM destination WHERE destination._id = :destinationId")
    public abstract Destination getDestinationInBackground(long destinationId);
}
