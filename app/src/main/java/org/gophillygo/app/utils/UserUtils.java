package org.gophillygo.app.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.preference.PreferenceManager;

import org.gophillygo.app.R;

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
            // If UUID was empty, either user just first installed the app, or reset its data
            // from system settings (unlikely the latter).
            showFirstInstallDialog(context);
        }

        return uuid;
    }

    private static void showFirstInstallDialog(Context context) {
        Log.d(LOG_LABEL, "show first app install dialog to ask for logging permissions");

        // set the theme via ContextThemeWrapper, or else the message does not show
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.GpgAlertDialogTheme));
        builder.setTitle(context.getString(R.string.first_launch_dialog_title))
                .setMessage(context.getString(R.string.first_launch_dialog_message))
                .setPositiveButton(context.getString(R.string.first_launch_dialog_ok_action), (dialog, which) -> {
                    Log.d(LOG_LABEL, "Permissions for data sharing approved; changing");
                    enableUserDataPosting(context);
                    dialog.dismiss();
                }).setNegativeButton(context.getString(R.string.first_launch_dialog_cancel_action), (dialog, which) -> {
                    Log.d(LOG_LABEL, "Permissions for data sharing not approved; cancelling");
                    dialog.cancel();
                });
        builder.create().show();
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
     * Update preferences to indicate that user has approved of sharing data with GoPhillyGo/Fabric.
     *
     * @param context Context for getting preferences and strings
     */
    private static void enableUserDataPosting(Context context) {
        Log.d(LOG_LABEL, "Updating user preferences to allow data sharing with GoPhillyGo/Fabric");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.general_preferences_send_flags_key), true);
        editor.putBoolean(context.getString(R.string.general_preferences_fabric_logging_key), true);

        // using `apply` instead of `commit` to fire off asynchronous commit and ignore result
        editor.apply();

    }

    /**
     * Check shared preferences to see if user allows for posting user flags to the GoPhillyGo server.
     *
     * @param context Context for getting shared preferences
     * @return True if flag posting allowed (defaults to false, for opt-in)
     */
    public static boolean isFlagPostingEnabled(Context context) {
        String key = context.getString(R.string.general_preferences_send_flags_key);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.contains(key) && sharedPreferences.getBoolean(key, false);
    }

    /**
     * Check if user allows for posting anonymized crash and usage data to Fabric.
     *
     * @param context For getting strings and preferences
     * @return True if Fabric may be enabled
     */
    public static boolean isFabricEnabled(Context context) {
        String key = context.getString(R.string.general_preferences_fabric_logging_key);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !sharedPreferences.contains(key) || !sharedPreferences.getBoolean(key, false);
    }
}
