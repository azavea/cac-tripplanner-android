package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;

import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.networkresource.ApiResponse;

import java.util.List;

import retrofit2.http.GET;

/**
 * Use Retrofit to query the server API for destinations.
 */

public interface DestinationWebservice {
    @GET("https://gophillygo.org/api/destinations/search?text=")
    LiveData<ApiResponse<List<Destination>>> getDestinations();
}
