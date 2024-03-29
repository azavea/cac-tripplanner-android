package org.gophillygo.app.data;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.CategoryAttraction;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.data.models.Event;
import org.gophillygo.app.data.models.EventInfo;
import org.gophillygo.app.data.models.UserFlagPost;
import org.gophillygo.app.data.models.UserFlagPostResponse;
import org.gophillygo.app.data.networkresource.AttractionNetworkBoundResource;
import org.gophillygo.app.data.networkresource.Resource;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

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

public class DestinationRepository {

    public interface CategoryAttractionCallback {
        void gotCategoryAttractions(LiveData<List<CategoryAttraction>> categoryAttractions);
    }

    private static final String LOG_LABEL = "DestinationRepository";

    private final DestinationWebservice webservice;
    private final AttractionFlagDao attractionFlagDao;
    private final DestinationDao destinationDao;
    private final EventDao eventDao;

    private final Executor backgroundExecutor;
    private final Handler mainThreadHandler;

    @Inject
    public DestinationRepository(DestinationWebservice webservice,
                                 AttractionFlagDao attractionFlagDao,
                                 DestinationDao destinationDao,
                                 EventDao eventDao,
                                 ExecutorService backgroundExecutor,
                                 Handler mainThreadHandler) {
        this.webservice = webservice;
        this.attractionFlagDao = attractionFlagDao;
        this.destinationDao = destinationDao;
        this.eventDao = eventDao;
        this.backgroundExecutor = backgroundExecutor;
        this.mainThreadHandler = mainThreadHandler;
    }

    public LiveData<DestinationInfo> getDestination(long destinationId) {
        // return a LiveData item directly from the database.
        return destinationDao.getDestination(destinationId);
    }

    public LiveData<EventInfo> getEvent(long eventId) {
        return eventDao.getEvent(eventId);
    }

    public LiveData<List<EventInfo>> getEventsForDestination(long destinationId) {
        return eventDao.getEventsForDestination(destinationId);
    }

    @SuppressLint("StaticFieldLeak")
    public void updateDestination(Destination destination) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                destinationDao.update(destination);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public void updateEvent(Event event) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                eventDao.update(event);
            }
        });
    }

    /**
     * Post user flag and anonymized app ID to GoPhillyGo server on change.
     *
     *
     * @param flag new flag set
     * @param userUuid user's anonymous app install identifier
     * @param apiKey key server expects on POST
     * @param postToServer True if user allows flags to be shared with GoPhillyGo
     */
    @SuppressLint("StaticFieldLeak")
    public void updateAttractionFlag(AttractionFlag flag, String userUuid, String apiKey, boolean postToServer) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                attractionFlagDao.save(flag);
            }
        });

        if (!postToServer) {
            Log.d(LOG_LABEL, "Posting user flags to server disabled; not sending");
            return;
        }

        UserFlagPost post = new UserFlagPost(flag.getAttractionID(), flag.getOption().apiName,
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
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                destinationDao.bulkUpdate(destinations);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public void updateMultipleEvents(List<Event> events) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                eventDao.bulkUpdate(events);
            }
        });
    }

    public LiveData<Resource<List<DestinationInfo>>> loadDestinations() {
        return new AttractionNetworkBoundResource<Destination, DestinationInfo>(webservice, destinationDao, eventDao, backgroundExecutor, mainThreadHandler) {
            @NonNull
            @Override
            protected LiveData<List<DestinationInfo>> loadFromDb() {
                return destinationDao.getAll();
            }
        }.getAsLiveData();
    }

    public LiveData<Resource<List<EventInfo>>> loadEvents() {
        return new AttractionNetworkBoundResource<Event, EventInfo>(webservice, destinationDao, eventDao, backgroundExecutor, mainThreadHandler) {
            @NonNull @Override
            protected LiveData<List<EventInfo>> loadFromDb() {
                return eventDao.getAll();
            }
        }.getAsLiveData();
    }

    @SuppressLint("StaticFieldLeak")
    public void loadCategoryAttractions(CategoryAttractionCallback callback) {
        Log.d(LOG_LABEL, "going to load category attractions");
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LiveData<List<CategoryAttraction>> categories = destinationDao.getCategoryImages();
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(LOG_LABEL, "finished loading category attractions");
                        callback.gotCategoryAttractions(categories);
                    }
                });
            }
        });
    }
}
