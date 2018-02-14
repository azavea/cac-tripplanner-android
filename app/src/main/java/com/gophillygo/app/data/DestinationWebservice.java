package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;

import com.gophillygo.app.data.models.DestinationQueryResponse;
import com.gophillygo.app.data.networkresource.ApiResponse;

import retrofit2.http.GET;

/**
 * Use Retrofit to query the server API for destinations.
 */

public interface DestinationWebservice {
    @GET("api/destinations/search?text=")
    LiveData<ApiResponse<DestinationQueryResponse>> getDestinations();
}
