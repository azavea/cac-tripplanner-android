package com.gophillygo.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gophillygo.app.R;

import java.util.UUID;

public class UserUuidUtils {

    private static final String LOG_LABEL = "UserUUidUtils";
    private final Context context;

    public UserUuidUtils(Context context) {
        this.context = context;
    }

    /**
     * Get the randomly generated ID for this app install from Shared Preferences, or create and
     * put one there if it does not yet exist, which can happen if this is the first app install,
     * or if the shared preferences file was erased (i.e., by the user clearing app settings.)
     *
     * @return String representation of a UUID
     */
    public String getUserUuid() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.user_uuid_shared_preferences_file),
                Context.MODE_PRIVATE);

        String preferencesKey = context.getString(R.string.user_uuid_shared_preferences_key);
        String uuid = sharedPreferences.getString(preferencesKey, "");
        if (uuid.isEmpty()) {
            // store a new UUID
            uuid = getRandomUuid();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(preferencesKey, uuid);
            // using `apply` instead of `commit` to fire off asynchronous commit and ignore result
            editor.apply();
            Log.w(LOG_LABEL, "Creating new UUID for this app install: " + uuid);
        }

        return uuid;
    }

    private String getRandomUuid() {
        return UUID.randomUUID().toString();
    }

}
