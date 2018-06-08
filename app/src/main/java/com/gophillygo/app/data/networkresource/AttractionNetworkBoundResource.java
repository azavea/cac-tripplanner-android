package com.gophillygo.app.data.networkresource;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gophillygo.app.data.DestinationDao;
import com.gophillygo.app.data.DestinationWebservice;
import com.gophillygo.app.data.EventDao;
import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationCategories;
import com.gophillygo.app.data.models.DestinationQueryResponse;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.models.Filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Shared network query manager between events and destinations (attractions).
 * Subclass to get LiveData responses for either destinations or events.
 */

abstract public class AttractionNetworkBoundResource<A extends Attraction, I extends AttractionInfo<A>>
        extends NetworkBoundResource<List<I>, DestinationQueryResponse> {

    // maximum rate at which to refresh data from network
    private static final long RATE_LIMIT = TimeUnit.MINUTES.toMillis(15);

    private final DestinationWebservice webservice;
    private final DestinationDao destinationDao;
    private final EventDao eventDao;

    public AttractionNetworkBoundResource(DestinationWebservice webservice,
                                          DestinationDao destinationDao,
                                          EventDao eventDao) {
        this.webservice = webservice;
        this.destinationDao = destinationDao;
        this.eventDao = eventDao;
    }

    @Override
    protected void saveCallResult(@NonNull DestinationQueryResponse response) {
        Long timestamp = System.currentTimeMillis();

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
                item.setCategoryFlags(new DestinationCategories(categories.contains(Filter.NATURE_CATEGORY),
                        categories.contains(Filter.EXERCISE_CATEGORY),
                        categories.contains(Filter.EDUCATIONAL_CATEGORY)));
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
