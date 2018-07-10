package org.gophillygo.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;

import org.gophillygo.app.R;
import org.gophillygo.app.tasks.AddGeofenceWorker;
import org.gophillygo.app.tasks.AddRemoveGeofencesBroadcastReceiver;

public class GpgPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_LABEL = "PreferenceFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
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
        Log.d(LOG_LABEL, "shared preference changed");

        final String notificationsKey = getString(R.string.general_preferences_allow_notifications_key);
        if (key.equals(notificationsKey)) {
            // Whether notifications have been enabled or disabled, we do the same thing:
            // kick off a worker that will check the setting before adding back or removing all
            // geofences for the places and events flagged 'want to go'.
            Activity activity = getActivity();
            Intent intent = new Intent(activity.getApplicationContext(), AddRemoveGeofencesBroadcastReceiver.class);
            intent.setAction(AddGeofenceWorker.ACTION_GEOFENCE_TRANSITION);
            activity.sendBroadcast(intent);
        } else {
            String message = "Unrecognized user preference changed: " + key;
            Log.w(LOG_LABEL, message);
            Crashlytics.log(message);
        }
    }
}
