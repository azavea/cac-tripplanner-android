package org.gophillygo.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import org.gophillygo.app.R;
import org.gophillygo.app.tasks.AddGeofenceWorker;
import org.gophillygo.app.tasks.AddRemoveGeofencesBroadcastReceiver;
import org.gophillygo.app.utils.UserUtils;

public class GpgPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_LABEL = "PreferenceFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // reset user ID and show message when setting for that is clicked
        Preference reset = findPreference(getString(R.string.general_preferences_reset_uuid_key));
        reset.setOnPreferenceClickListener(preference -> {
            String uuid = UserUtils.resetUuid(getActivity());
            String message = getString(R.string.general_preferences_reset_uuid_message, uuid);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            return true;
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // remove dividers: https://stackoverflow.com/a/27952333
        View rootView = getView();
        ListView list = rootView.findViewById(android.R.id.list);
        list.setDivider(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!isAdded() || getActivity() == null) {
            return;
        }
        Log.d(LOG_LABEL, "shared preference changed");

        final String notificationsKey = getString(R.string.general_preferences_allow_notifications_key);
        final String userFlagsKey = getString(R.string.general_preferences_send_flags_key);
        final String fabricKey = getString(R.string.general_preferences_fabric_logging_key);

        if (key.equals(notificationsKey)) {
            // Whether notifications have been enabled or disabled, we do the same thing:
            // kick off a worker that will check the setting before adding back or removing all
            // geofences for the places and events flagged 'want to go'.
            Activity activity = getActivity();
            Intent intent = new Intent(activity.getApplicationContext(), AddRemoveGeofencesBroadcastReceiver.class);
            intent.setAction(AddGeofenceWorker.ACTION_GEOFENCE_TRANSITION);
            activity.sendBroadcast(intent);
        } else if (key.equals(userFlagsKey)) {
            Log.d(LOG_LABEL, "toggled user flags upload user setting");
        } else if (key.equals(fabricKey)) {
            Log.d(LOG_LABEL, "toggled user setting for Fabric logging");
            // notify user to restart app for Fabric to stop logging, if setting changed to disable it
            if (!sharedPreferences.getBoolean(fabricKey, false)) {
                Toast.makeText(getActivity(), getString(R.string.general_preferences_fabric_logging_disabled_notification), Toast.LENGTH_LONG).show();
            }
        } else {
            String message = "Unrecognized user preference changed: " + key;
            Log.w(LOG_LABEL, message);
            Crashlytics.log(message);
        }
    }
}
