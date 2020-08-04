package org.gophillygo.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.gophillygo.app.R;
import org.gophillygo.app.tasks.AddGeofenceWorker;
import org.gophillygo.app.tasks.AddRemoveGeofencesBroadcastReceiver;
import org.gophillygo.app.utils.UserUtils;

import java.util.Objects;

public class GpgPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_LABEL = "PreferenceFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // reset user ID and show message when setting for that is clicked
        Preference reset = findPreference(getString(R.string.general_preferences_reset_uuid_key));
        Objects.requireNonNull(reset).setOnPreferenceClickListener(preference -> {
            String uuid = UserUtils.resetUuid(requireActivity());
            String message = getString(R.string.general_preferences_reset_uuid_message, uuid);
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            return true;
        });
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
            Toast.makeText(getActivity(), getString(R.string.general_preferences_fabric_logging_disabled_notification), Toast.LENGTH_LONG).show();
            // Delete any unsent crash reports collected while logging was disabled,
            // or else they will send on next app start.
            Log.d(LOG_LABEL, "Delete any unsent crash reports (logging opt-in setting changed)");
            FirebaseCrashlytics.getInstance().deleteUnsentReports();
        } else {
            String message = "Unrecognized user preference changed: " + key;
            Log.w(LOG_LABEL, message);
            FirebaseCrashlytics.getInstance().log(message);
        }
    }
}
