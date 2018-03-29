package com.gophillygo.app.data;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.networkresource.AttractionNetworkBoundResource;
import com.gophillygo.app.data.networkresource.Resource;

import java.util.List;

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

    public LiveData<Event> getEvent(long eventId) {
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

    public LiveData<Resource<List<Destination>>> loadDestinations() {
        return new AttractionNetworkBoundResource<Destination>(webservice, destinationDao, eventDao) {
            @NonNull
            @Override
            protected LiveData<List<Destination>> loadFromDb() {
                return destinationDao.getAll();
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<Event>>> loadEvents() {
        return new AttractionNetworkBoundResource<Event>(webservice, destinationDao, eventDao) {
            @NonNull @Override
            protected LiveData<List<Event>> loadFromDb() {
                return eventDao.getAll();
            }
        }.getAsLiveData();
    }
}
