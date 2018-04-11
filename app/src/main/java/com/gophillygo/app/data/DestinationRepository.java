package com.gophillygo.app.data;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.AttractionFlag;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.networkresource.AttractionNetworkBoundResource;
import com.gophillygo.app.data.networkresource.Resource;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static android.arch.lifecycle.Transformations.*;

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
    public void updateAttractionFlag(AttractionFlag flag) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (flag.getId() != null) {
                    attractionFlagDao.update(flag);
                } else {
                    attractionFlagDao.save(flag);
                }
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
        LiveData<Resource<List<Destination>>> data = new AttractionNetworkBoundResource<Destination>(webservice, destinationDao, eventDao) {
            @NonNull
            @Override
            protected LiveData<List<Destination>> loadFromDb() {
                return destinationDao.getAll();
            }
        }.getAsLiveData();
        return addAttractionFlags(data, false);
    }

    public LiveData<Resource<List<Event>>> loadEvents() {
        LiveData<Resource<List<Event>>> data = new AttractionNetworkBoundResource<Event>(webservice, destinationDao, eventDao) {
            @NonNull @Override
            protected LiveData<List<Event>> loadFromDb() {
                return eventDao.getAll();
            }
        }.getAsLiveData();
        return addAttractionFlags(data, true);
    }

    private <T extends Attraction> LiveData<Resource<List<T>>> addAttractionFlags (LiveData<Resource<List<T>>> data,
                                                                                   boolean isEvent) {
        return switchMap(data, attractions -> {
            MutableLiveData<Resource<List<T>>> result = new MutableLiveData<>();
            if (attractions == null || attractions.data == null) {
                result.postValue(attractions);
                return result;
            }

            return switchMap(attractionFlagDao.getAttractionFlags(isEvent), flags -> {
                if (flags == null) {
                    result.postValue(attractions);
                    return result;
                }

                SparseArray<AttractionFlag> flagMap = new SparseArray<>();
                for (AttractionFlag flag : flags) {
                    flagMap.put(flag.getAttractionID(), flag);
                }
                for (T attraction : attractions.data) {
                    attraction.setFlag(flagMap.get(attraction.getId()));
                }
                result.postValue(attractions);
                return result;
            });
        });
    }
}