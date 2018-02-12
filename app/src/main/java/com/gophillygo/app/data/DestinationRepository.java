package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationQueryResponse;
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
    private static final String LOG_LABEL = "DestinationRepository";

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
        return new NetworkBoundResource<List<Destination>, DestinationQueryResponse>() {
            @Override
            protected void saveCallResult(@NonNull DestinationQueryResponse response) {
                // clear out existing database entries before adding new ones
                dao.clear();
                for (Destination item: response.getDestinations()) {
                    dao.save(item);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Destination> data) {
                Log.d(LOG_LABEL, "shouldFetch");
                return data == null || data.isEmpty() || rateLimiter.shouldFetch(DESTINATIONS_KEY);
            }

            @NonNull @Override
            protected LiveData<List<Destination>> loadFromDb() {
                Log.d(LOG_LABEL, "loadFromDb");
                return dao.getAll();
            }

            @NonNull @Override
            protected LiveData<ApiResponse<DestinationQueryResponse>> createCall() {
                Log.d(LOG_LABEL, "createCall");
                return webservice.getDestinations();
            }
        }.getAsLiveData();
    }
}
