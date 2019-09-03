package org.gophillygo.app.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.synnapps.carouselview.CarouselView;

import org.gophillygo.app.DetailCarouselViewListener;
import org.gophillygo.app.R;
import org.gophillygo.app.data.models.Attraction;
import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.data.models.DestinationLocation;
import org.gophillygo.app.data.models.EventInfo;
import org.gophillygo.app.tasks.AddRemoveGeofencesBroadcastReceiver;
import org.gophillygo.app.tasks.RemoveGeofenceWorker;
import org.gophillygo.app.utils.UserUtils;

import io.fabric.sdk.android.Fabric;

public abstract class AttractionDetailActivity extends AppCompatActivity {

    public static final String NOTIFICATION_ID_KEY = "launching_notification_id";
    public static final String GEOFENCE_ID_KEY = "launching_geofence_id";

    protected static final int COLLAPSED_LINE_COUNT = 4;
    protected static final int EXPANDED_MAX_LINES = 50;
    private static final String LOG_LABEL = "AttractionDetail";

    protected DestinationInfo destinationInfo;
    protected String userUuid;
    public View.OnClickListener toggleClickListener;

    protected abstract Class getMapActivity();
    protected abstract int getAttractionId();

    protected void addOrRemoveGeofence(AttractionInfo info, Boolean haveExistingGeofence, Boolean settingGeofence) {
        if (settingGeofence) {
            if (haveExistingGeofence) {
                Log.d(LOG_LABEL, "No change to geofence");
                return;
            }
            // add geofence
            Log.d(LOG_LABEL, "Add attraction geofence");
            if (info instanceof EventInfo) {
                AddRemoveGeofencesBroadcastReceiver.addOneGeofence((EventInfo)info);
            } else if (info instanceof DestinationInfo) {
                AddRemoveGeofencesBroadcastReceiver.addOneGeofence(((DestinationInfo) info).getDestination());
            }
        } else if (haveExistingGeofence) {
            Log.d(LOG_LABEL, "Removing attraction geofence");
            RemoveGeofenceWorker.removeOneGeofence(info);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fabric/Crashlytics crash and usage data logging.
        // Disable if user setting turned off; still must be initialized to avoid errors.
        // Based on: https://stackoverflow.com/a/31996615
        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(UserUtils.isFabricEnabled(this)).build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        // Get or create unique, random UUID for app install for posting user flags
        userUuid = UserUtils.getUserUuid(getApplicationContext());
        Crashlytics.setUserIdentifier(userUuid);
        toggleClickListener = v -> {
            // click handler for toggling expanding/collapsing description card
            TextView descriptionView = findViewById(R.id.detail_description_text);

            TextView view = (TextView) v;
            int current = descriptionView.getMaxLines();
            if (current == COLLAPSED_LINE_COUNT) {
                descriptionView.setMaxLines(EXPANDED_MAX_LINES);
                // make links clickable in expanded view
                descriptionView.setMovementMethod(LinkMovementMethod.getInstance());
                view.setText(R.string.detail_description_collapse);
            } else {
                descriptionView.setMaxLines(COLLAPSED_LINE_COUNT);
                descriptionView.setEllipsize(TextUtils.TruncateAt.END);
                // disable clicking links to also disable scrolling
                descriptionView.setMovementMethod(null);
                // must reset click listener after un-setting movement method
                v.setOnClickListener(toggleClickListener);
                // set text again, to make ellipsize run
                descriptionView.setText(descriptionView.getText());
                view.setText(R.string.detail_description_expand);
            }
        };

        // cancel launching notification, if any
        if (getIntent().hasExtra(NOTIFICATION_ID_KEY)) {
            String notificationId = getIntent().getStringExtra(NOTIFICATION_ID_KEY);
            int geofenceId = getIntent().getIntExtra(GEOFENCE_ID_KEY, -1);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            if (notificationId != null && !notificationId.isEmpty() && geofenceId > -1) {
                notificationManager.cancel(notificationId, geofenceId);
                Log.d(LOG_LABEL, "Closing notification after launching detail view");
            }
        }

    }

    // open map when user clicks map button
    public void goToMap(View view) {
        DestinationLocation loc = destinationInfo.getDestination().getLocation();
        Intent mapIntent = new Intent(this, getMapActivity());
        mapIntent.putExtra(MapsActivity.X, loc.getX());
        mapIntent.putExtra(MapsActivity.Y, loc.getY());
        mapIntent.putExtra(MapsActivity.ATTRACTION_ID, getAttractionId());
        startActivity(mapIntent);
    }

    // open the GoPhillyGo website, passing the destination, when "get directions" clicked
    public void goToDirections(View view) {
        // pass parameters destination and destinationText to https://gophillygo.org/
        Uri directionsUri = new Uri.Builder().scheme("https").authority("gophillygo.org")
                .appendQueryParameter("origin", "")
                .appendQueryParameter("originText", "")
                .appendQueryParameter("destination", destinationInfo.getDestination().getLocation().toString())
                .appendQueryParameter("destinationText", destinationInfo.getDestination().getAddress()).build();
        Intent intent = new Intent(Intent.ACTION_VIEW, directionsUri);
        startActivity(intent);
    }

    // open website for destination in browser
    public void goToWebsite(View view) {
        String url = destinationInfo.getDestination().getWebsiteUrl();
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else {
            String message = "Not opening website for attraction because it is missing a link";
            Log.e(LOG_LABEL, message);
            Crashlytics.log(message);
        }

    }

    public Drawable getFlagImage(AttractionInfo attractionInfo) {
        if (attractionInfo == null) return null;

        return ContextCompat.getDrawable(this, attractionInfo.getFlagImage());
    }

    public void setupCarousel(CarouselView carouselView, Attraction attraction) {
        carouselView.setViewListener(new DetailCarouselViewListener(this) {
            @Override
            public String getImageUrlAt(int position) {
                String url = null;
                if (position == 0) {
                    url = attraction.getWideImage();
                } else if (position <= attraction.getExtraWideImages().size()) {
                    url = attraction.getExtraWideImages().get(position - 1);
                }
                // Shouldn't be possible to reach this, but re-use the wide image if the extra images are blank
                // or there aren't enough of them
                if (url == null || url.equals("")) {
                    Log.e(LOG_LABEL, "Unexpected missing extra image for attraction: " + attraction.getId());
                    url = attraction.getWideImage();
                }
                return url;
            }
        });
        carouselView.setPageCount(attraction.getExtraWideImages().size() + 1);
    }

    /**
     * Gets the user-presentable detail string for a user flag (been, want to go, etc.)
     *
     * @param info AttractionInfo object for a place or event
     * @return String from string resources; varies for places and events
     */
    public String getFlagLabel(AttractionInfo info) {
        if (info == null || info.getAttraction() == null) {
            return getString(R.string.place_detail_unset);
        }

        AttractionFlag flag = info.getFlag();
        getString(R.string.place_detail_unset);
        return info.getAttraction().isEvent() ?
                getString(flag.getOption().eventLabel) :
                getString(flag.getOption().placeLabel);
    }
}
