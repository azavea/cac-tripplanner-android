package com.gophillygo.app.data;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationQueryResponse;
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
    private final DestinationDao dao;

    // maximum rate at which to refresh data from network
    private static final long RATE_LIMIT = TimeUnit.MINUTES.toMillis(15);

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

    @SuppressLint("StaticFieldLeak")
    public void updateDestination(Destination destination) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                dao.update(destination);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void updateMultipleDestinations(List<Destination> destinations) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                dao.bulkUpdate(destinations);
                return null;
            }
        }.execute();
    }

    public LiveData<Resource<List<Destination>>> loadDestinations() {
        return new NetworkBoundResource<List<Destination>, DestinationQueryResponse>() {
            @Override
            protected void saveCallResult(@NonNull DestinationQueryResponse response) {
                // clear out existing database entries before adding new ones
                dao.clear();
                for (Destination item: response.getDestinations()) {
                    item.setTimestamp(System.currentTimeMillis());
                    dao.save(item);
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
                return dao.getAll();
            }

            @NonNull @Override
            protected LiveData<ApiResponse<DestinationQueryResponse>> createCall() {
                return webservice.getDestinations();
            }
        }.getAsLiveData();
    }
}
