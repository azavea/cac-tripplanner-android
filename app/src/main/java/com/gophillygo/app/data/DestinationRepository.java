package com.gophillygo.app.data;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationQueryResponse;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.networkresource.ApiResponse;
import com.gophillygo.app.data.networkresource.NetworkBoundResource;
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

class DestinationRepository {
    private static final String LOG_LABEL = "DestinationRepository";

    private final DestinationWebservice webservice;
    private final DestinationDao destinationDao;
    private final EventDao eventDao;

    // maximum rate at which to refresh data from network
    private static final long RATE_LIMIT = TimeUnit.MINUTES.toMillis(15);

    @Inject
    public DestinationRepository(DestinationWebservice webservice,
                                 DestinationDao destinationDao,
                                 EventDao eventDao) {
        this.webservice = webservice;
        this.destinationDao = destinationDao;
        this.eventDao = eventDao;
    }

    public LiveData<Destination> getDestination(long destinationId) {
        // return a LiveData item directly from the database.
        return destinationDao.getDestination(destinationId);
    }

    @SuppressLint("StaticFieldLeak")
    public void updateDestination(Destination destination) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                destinationDao.update(destination);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void updateMultipleDestinations(List<Destination> destinations) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                destinationDao.bulkUpdate(destinations);
                return null;
            }
        }.execute();
    }

    public LiveData<Resource<List<Destination>>> loadDestinations() {
        return new NetworkBoundResource<List<Destination>, DestinationQueryResponse>() {
            @Override
            protected void saveCallResult(@NonNull DestinationQueryResponse response) {
                Long timestamp = System.currentTimeMillis();

                // clear out existing database entries before adding new ones
                destinationDao.clear();
                eventDao.clear();

                // save destinations
                for (Destination item: response.getDestinations()) {
                    item.setTimestamp(timestamp);
                    destinationDao.save(item);
                }

                // save events
                for (Event item: response.getEvents()) {
                    item.setTimestamp(timestamp);
                    eventDao.save(item);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Destination> data) {
                if (data == null || data.isEmpty()) {
                    return true;
                }
                Destination first = data.get(0);
                return System.currentTimeMillis() - first.getTimestamp() > RATE_LIMIT;
            }

            @NonNull @Override
            protected LiveData<List<Destination>> loadFromDb() {
                return destinationDao.getAll();
            }

            @NonNull @Override
            protected LiveData<ApiResponse<DestinationQueryResponse>> createCall() {
                return webservice.getDestinations();
            }
        }.getAsLiveData();
    }
}
