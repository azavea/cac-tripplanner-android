package org.gophillygo.app.data.networkresource;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Response;

/**
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample/app/src/main/java/com/android/example/github/api/ApiResponse.java
 *
 * Common class used by API responses.
 *
 * @param <T>
 */

public class ApiResponse<T> {
    private static final String LOG_LABEL = "ApiResponse";

    @SuppressWarnings("WeakerAccess")
    public final int code;
    @Nullable
    public final T body;
    @Nullable
    public final String errorMessage;

    public ApiResponse(Throwable error) {
        Log.e(LOG_LABEL, "API response error:");
        Log.e(LOG_LABEL, Objects.requireNonNull(error.getMessage()));
        code = 500;
        body = null;
        errorMessage = error.getMessage();
    }

    @SuppressWarnings("ConstantConditions")
    public ApiResponse(Response<T> response) {
        code = response.code();
        if (response.isSuccessful()) {
            body = response.body();
            Log.w(LOG_LABEL, "Response is successful");
            Log.w(LOG_LABEL, String.valueOf(response.code()));
            Log.d(LOG_LABEL, body.toString());
            errorMessage = "";
        } else {
            Log.e(LOG_LABEL, "Request failed");
            Log.e(LOG_LABEL, String.valueOf(response.code()));
            Log.e(LOG_LABEL, response.raw().body().toString());
            String message = "";
            if (response.errorBody() != null) {
                try {
                    message = response.errorBody().string();
                } catch (IOException ex) {
                    Log.e(LOG_LABEL, "Error parsing API response");
                }
            }
            if (message.trim().isEmpty()) {
                message = response.message();
            }
            errorMessage = message;
            body = null;
        }
    }

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }

}

