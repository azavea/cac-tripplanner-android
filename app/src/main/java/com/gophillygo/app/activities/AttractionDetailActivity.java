package com.gophillygo.app.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.gophillygo.app.R;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.DestinationInfo;

abstract class AttractionDetailActivity extends AppCompatActivity {
    protected static final int COLLAPSED_LINE_COUNT = 4;
    protected static final int EXPANDED_MAX_LINES = 50;
    protected DestinationInfo destinationInfo;

    protected View.OnClickListener toggleClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // must reset click listener after unsetting movement method
                descriptionView.setOnClickListener(toggleClickListener);
                // set text again, to make ellipsize run
                descriptionView.setText(descriptionView.getText());
                view.setText(R.string.detail_description_expand);
            }
        };
    }

    // open map when user clicks map button
    public void goToMap(View view) {
        // TODO: #10 open within app map view, once implemented
        // for now, open Google Maps externally with a marker at the given location
        String locationString = destinationInfo.getDestination().getLocation().toString();
        Uri gmapsUri = Uri.parse("geo:" + locationString + "?q=" + locationString + "(" +
                destinationInfo.getDestination().getName() + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmapsUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    // open the GoPhillyGo website, passing the destination, when "get directions" clicked
    public void goToDirections(View view) {
        // pass parameters destination and destinationText to https://gophillygo.org/
        Uri directionsUri = new Uri.Builder().scheme("https").authority("gophillygo.org")
                // TODO: #9 send current user location as origin
                .appendQueryParameter("origin", "")
                .appendQueryParameter("originText", "")
                .appendQueryParameter("destination", destinationInfo.getDestination().getLocation().toString())
                .appendQueryParameter("destinationText", destinationInfo.getDestination().getAddress()).build();
        Intent intent = new Intent(Intent.ACTION_VIEW, directionsUri);
        startActivity(intent);
    }

    // open website for destination in browser
    public void goToWebsite(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(destinationInfo.getDestination().getWebsiteUrl()));
        startActivity(intent);
    }

    public Drawable getFlagImage(AttractionInfo attractionInfo) {
        if (attractionInfo == null) return null;

        return ContextCompat.getDrawable(this, attractionInfo.getFlagImage());
    }
}
