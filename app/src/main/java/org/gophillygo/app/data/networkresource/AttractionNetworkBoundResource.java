package org.gophillygo.app.data.networkresource;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import org.gophillygo.app.BuildConfig;
import org.gophillygo.app.data.DestinationDao;
import org.gophillygo.app.data.DestinationWebservice;
import org.gophillygo.app.data.EventDao;
import org.gophillygo.app.data.models.Attraction;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.CategoryAttraction;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.DestinationCategories;
import org.gophillygo.app.data.models.DestinationQueryResponse;
import org.gophillygo.app.data.models.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Shared network query manager between events and destinations (attractions).
 * Subclass to get LiveData responses for either destinations or events.
 */

abstract public class AttractionNetworkBoundResource<A extends Attraction, I extends AttractionInfo<A>>
        extends NetworkBoundResource<List<I>, DestinationQueryResponse> {

    // maximum rate at which to refresh data from network
    private static final long RATE_LIMIT = BuildConfig.DEBUG ? TimeUnit.MINUTES.toMillis(15):
            TimeUnit.HOURS.toMillis(12);

    private final DestinationWebservice webservice;
    private final DestinationDao destinationDao;
    private final EventDao eventDao;

    public AttractionNetworkBoundResource(DestinationWebservice webservice,
                                          DestinationDao destinationDao,
                                          EventDao eventDao, Executor executor, Handler handler) {
        super(executor, handler);
        this.webservice = webservice;
        this.destinationDao = destinationDao;
        this.eventDao = eventDao;
    }

    @Override
    protected void saveCallResult(@NonNull DestinationQueryResponse response) {
        long timestamp = System.currentTimeMillis();

        // clear out existing database entries before adding new ones
        destinationDao.clear();
        eventDao.clear();

        // save destinations
        List<Destination> destinations = response.getDestinations();
        Set<Integer> destinationIds = new HashSet<>(destinations.size());
        for (Destination item : destinations) {
            item.setTimestamp(timestamp);
            List<String> categories = item.getCategories();
            if (categories != null && !categories.isEmpty()) {
                item.setCategoryFlags(new DestinationCategories(categories.contains(CategoryAttraction.PlaceCategories.Nature.dbName),
                        categories.contains(CategoryAttraction.PlaceCategories.Exercise.dbName),
                        categories.contains(CategoryAttraction.PlaceCategories.Educational.dbName)));
            } else {
                item.setCategoryFlags(new DestinationCategories(false, false, false));
            }
            destinationIds.add(item.getId());
            destinationDao.save(item);
        }

        // save events
        for (Event item: response.getEvents()) {
            // Work-around for published event with unpublished destination
            if (item.getDestination() != null && !destinationIds.contains(item.getDestination())) {
                item.setDestination(null);
            }
            // Hide (first) destination for events with multiple destinations
            ArrayList<Destination> eventDestinations = item.getDestinations();
            if (eventDestinations != null && eventDestinations.size() > 1) {
                item.setDestination(null);
            }
            item.setTimestamp(timestamp);
            eventDao.save(item);
        }
    }

    @Override
    protected boolean shouldFetch(@Nullable List<I> data) {
        if (data == null || data.isEmpty()) {
            return true;
        }
        Attraction first = data.get(0).getAttraction();
        return System.currentTimeMillis() - first.getTimestamp() > RATE_LIMIT;
    }

    @NonNull @Override
    protected LiveData<ApiResponse<DestinationQueryResponse>> createCall() {
        return webservice.getDestinations();
    }

    @NonNull @Override
    abstract protected LiveData<List<I>> loadFromDb();
}
