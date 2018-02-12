package com.gophillygo.app.data.networkresource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.gophillygo.app.data.networkresource.Status.ERROR;
import static com.gophillygo.app.data.networkresource.Status.LOADING;
import static com.gophillygo.app.data.networkresource.Status.SUCCESS;

/**
 * Expose network status in a way that encapsulates both the data and its state.
 *
 * From:
 * https://developer.android.com/topic/libraries/architecture/guide.html
 * Also see code example at:
 * https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample/app/src/main/java/com/android/example/github/vo/Resource.java
 */

//a generic class that describes a data with a status
public class Resource<T> {
    @NonNull public final Status status;
    @Nullable public final T data;
    @Nullable public final String message;
    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, @Nullable T data) {
        return new Resource<>(ERROR, data, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(LOADING, data, null);
    }
}
