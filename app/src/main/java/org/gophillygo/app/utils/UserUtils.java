package org.gophillygo.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.gophillygo.app.R;

import java.util.Map;
import java.util.UUID;

public class UserUtils {

    private static final String LOG_LABEL = "UserUtils";

    // Do not instantiate
    private UserUtils() { }

    /**
     * Get the randomly generated ID for this app install from Shared Preferences, or create and
     * put one there if it does not yet exist, which can happen if this is the first app install,
     * or if the shared preferences file was erased (i.e., by the user clearing app settings.)
     *
     * @return String representation of a UUID
     */
    public static String getUserUuid(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.user_uuid_shared_preferences_file),
                Context.MODE_PRIVATE);

        String preferencesKey = context.getString(R.string.user_uuid_shared_preferences_key);
        String uuid = sharedPreferences.getString(preferencesKey, "");

        if (uuid.isEmpty()) {
            uuid = setNewUuid(sharedPreferences, preferencesKey);
        }

        return uuid;
    }

    /**
     * Store a new UUID for the app install.
     *
     * @param sharedPreferences Where the UUID will be stored
     * @param preferencesKey String identifier for the UUID in shared preferences
     * @return Newly generated UUID
     */
    private static String setNewUuid(SharedPreferences sharedPreferences, String preferencesKey) {
        String uuid = getRandomUuid();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferencesKey, uuid);
        // using `apply` instead of `commit` to fire off asynchronous commit and ignore result
        editor.apply();
        Log.w(LOG_LABEL, "Creating new UUID for this app install: " + uuid);
        return uuid;
    }

    public static String resetUuid(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.user_uuid_shared_preferences_file),
                Context.MODE_PRIVATE);

        String preferencesKey = context.getString(R.string.user_uuid_shared_preferences_key);
        return setNewUuid(sharedPreferences, preferencesKey);
    }

    private static String getRandomUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Check shared preferences to see if user allows for posting user flags to the GoPhillyGo server.
     *
     * @param context Context for getting shared preferences
     * @return True if flag posting allowed (defaults to true)
     */
    public static boolean isFlagPostingEnabled(Context context) {
        String key = context.getString(R.string.general_preferences_send_flags_key);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !sharedPreferences.contains(key) || sharedPreferences.getBoolean(key, true);
    }

}
