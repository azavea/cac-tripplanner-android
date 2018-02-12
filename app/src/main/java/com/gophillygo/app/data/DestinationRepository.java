package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.networkresource.ApiResponse;
import com.gophillygo.app.data.networkresource.NetworkBoundResource;
import com.gophillygo.app.data.networkresource.RateLimiter;
import com.gophillygo.app.data.networkresource.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Mediator between Destination persistent data store and web service.
 * Handles querying for data and loading it into the data store.
 *
 * Based on:
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */

public class DestinationRepository {
    private final static String DESTINATIONS_KEY = "destinations";
    private DestinationWebservice webservice;
    private DestinationDao dao;
    private RateLimiter<String> rateLimiter = new RateLimiter<>(10, TimeUnit.MINUTES);

    @Inject
    public DestinationRepository(DestinationWebservice webservice,
                                 DestinationDao dao) {
        this.webservice = webservice;
        this.dao = dao;
    }

    public LiveData<Destination> getDestination(String destinationId) {
        // return a LiveData item directly from the database.
        return dao.getDestination(destinationId);
    }

    public LiveData<Resource<List<Destination>>> loadDestinations() {
        return new NetworkBoundResource<List<Destination>, List<Destination>>() {
            @Override
            protected void saveCallResult(@NonNull List<Destination> items) {
                // clear out existing database entries before adding new ones
                dao.clear();
                for (Destination item: items) {
                    dao.save(item);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Destination> data) {
                return (data == null) && rateLimiter.shouldFetch(DESTINATIONS_KEY);
            }

            @NonNull @Override
            protected LiveData<List<Destination>> loadFromDb() {
                return dao.getAll();
            }

            @NonNull @Override
            protected LiveData<ApiResponse<List<Destination>>> createCall() {
                return webservice.getDestinations();
            }
        }.getAsLiveData();
    }
}
