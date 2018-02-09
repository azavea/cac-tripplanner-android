package com.gophillygo.app.data;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Use Retrofit to query the server API for destinations.
 */

public interface DestinationWebservice {
    @GET("/users/{user}")
    Call<Destination> getDestinations(@Path("destinations") String userId);
}
