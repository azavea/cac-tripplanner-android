package com.gophillygo.app.data;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gophillygo.app.data.models.AttractionFlag;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.models.EventInfo;
import com.gophillygo.app.data.models.UserFlagPost;
import com.gophillygo.app.data.models.UserFlagPostResponse;
import com.gophillygo.app.data.networkresource.AttractionNetworkBoundResource;
import com.gophillygo.app.data.networkresource.Resource;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private final AttractionFlagDao attractionFlagDao;
    private final DestinationDao destinationDao;
    private final EventDao eventDao;

    @Inject
    public DestinationRepository(DestinationWebservice webservice,
                                 AttractionFlagDao attractionFlagDao,
                                 DestinationDao destinationDao,
                                 EventDao eventDao) {
        this.webservice = webservice;
        this.attractionFlagDao = attractionFlagDao;
        this.destinationDao = destinationDao;
        this.eventDao = eventDao;
    }

    public LiveData<DestinationInfo> getDestination(long destinationId) {
        // return a LiveData item directly from the database.
        return destinationDao.getDestination(destinationId);
    }

    public LiveData<EventInfo> getEvent(long eventId) {
        return eventDao.getEvent(eventId);
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
    public void updateEvent(Event event) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                eventDao.update(event);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public void updateAttractionFlag(AttractionFlag flag, String userUuid, String apiKey) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                attractionFlagDao.save(flag);
                return null;
            }
        }.execute();

        UserFlagPost post = new UserFlagPost(flag.getAttractionID(), flag.getOption().api_name,
                flag.isEvent(), userUuid, apiKey);

        webservice.postUserFlag(post).enqueue(new Callback<UserFlagPostResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserFlagPostResponse> call,
                                   @NonNull Response<UserFlagPostResponse> response) {
                Log.d(LOG_LABEL, "User flag posted: " + response.message());
            }

            @Override
            public void onFailure(@NonNull Call<UserFlagPostResponse> call, @NonNull Throwable t) {
                Log.e(LOG_LABEL, "Request to POST user flag failed: " + t.toString());
            }
        });

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

    @SuppressLint("StaticFieldLeak")
    public void updateMultipleEvents(List<Event> events) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                eventDao.bulkUpdate(events);
                return null;
            }
        }.execute();
    }

    public LiveData<Resource<List<DestinationInfo>>> loadDestinations() {
        return new AttractionNetworkBoundResource<Destination, DestinationInfo>(webservice, destinationDao, eventDao) {
            @NonNull
            @Override
            protected LiveData<List<DestinationInfo>> loadFromDb() {
                return destinationDao.getAll();
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<EventInfo>>> loadEvents() {
        return new AttractionNetworkBoundResource<Event, EventInfo>(webservice, destinationDao, eventDao) {
            @NonNull @Override
            protected LiveData<List<EventInfo>> loadFromDb() {
                return eventDao.getAll();
            }
        }.getAsLiveData();
    }
}
