package org.gophillygo.app.data.networkresource;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.concurrent.Executor;


/**
 * Based on:
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */


public abstract class NetworkBoundResource<ResultType, RequestType> {

    private static final String LOG_LABEL = "NetworkBound";

    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();

    private final Executor executor;
    private final Handler resultHandler;

    // Called to save the result of the API response into the database
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestType item);

    // Called with the data in the database to decide whether it should be
    // fetched from the network.
    @MainThread
    protected abstract boolean shouldFetch(@Nullable ResultType data);

    // Called to get the cached data from the database
    @NonNull @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    // Called to create the API call.
    @NonNull
    @MainThread
    protected abstract LiveData<ApiResponse<RequestType>> createCall();

    // Called when the fetch fails. The child class may want to reset components
    // like rate limiter.
    @SuppressWarnings("WeakerAccess")
    @MainThread
    protected void onFetchFailed() {
        Log.w(LOG_LABEL, "fetch request failed");
    }

    @MainThread
    protected NetworkBoundResource(Executor executor, Handler resultHandler) {
        this.executor = executor;
        this.resultHandler = resultHandler;
        result.setValue(Resource.loading(null));
        LiveData<ResultType> dbSource = loadFromDb();
        result.addSource(dbSource, data -> {
            result.removeSource(dbSource);
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource);
            } else {
                result.addSource(dbSource,
                        newData -> result.setValue(Resource.success(newData)));
            }
        });
    }

    private void fetchFromNetwork(final LiveData<ResultType> dbSource) {
        LiveData<ApiResponse<RequestType>> apiResponse = createCall();
        // we re-attach dbSource as a new source,
        // it will dispatch its latest value quickly
        result.addSource(dbSource,
                newData -> result.setValue(Resource.loading(newData)));
        result.addSource(apiResponse, response -> {
            result.removeSource(apiResponse);
            result.removeSource(dbSource);
            if (response.isSuccessful()) {
                saveResultAndReInit(response);
            } else {
                onFetchFailed();
                result.addSource(dbSource,
                        newData -> result.setValue(
                                Resource.error(response.errorMessage, newData)));
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    @MainThread
    private void saveResultAndReInit(ApiResponse<RequestType> response) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                if (response.body != null) {
                    saveCallResult(response.body);
                } else {
                    Log.w(LOG_LABEL, "Received null API response");
                }
                reInit();
            }
        });
    }

    @WorkerThread
    private void reInit(){
        resultHandler.post(new Runnable() {
            @Override
            public void run() {
                result.addSource(loadFromDb(),
                        newData -> result.setValue(Resource.success(newData)));
            }
        });
    }

    public final LiveData<Resource<ResultType>> getAsLiveData() {
        return result;
    }
}

