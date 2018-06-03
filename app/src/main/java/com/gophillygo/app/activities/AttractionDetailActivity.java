package com.gophillygo.app.activities;

import android.arch.persistence.room.util.StringUtil;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Attraction;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.DestinationLocation;
import com.synnapps.carouselview.CarouselView;

abstract class AttractionDetailActivity extends AppCompatActivity {
    protected static final int COLLAPSED_LINE_COUNT = 4;
    protected static final int EXPANDED_MAX_LINES = 50;
    private static final String LOG_LABEL = "AttractionDetail";
    protected DestinationInfo destinationInfo;

    protected View.OnClickListener toggleClickListener;

    protected abstract Class getMapActivity();
    protected abstract int getAttractionId();

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
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(destinationInfo.getDestination().getWebsiteUrl()));
        startActivity(intent);
    }

    public Drawable getFlagImage(AttractionInfo attractionInfo) {
        if (attractionInfo == null) return null;

        return ContextCompat.getDrawable(this, attractionInfo.getFlagImage());
    }

    public void setupCarousel(CarouselView carouselView, Attraction attraction) {
        carouselView.setImageListener((position, imageView) -> {
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
            Glide.with(this).load(url).into(imageView);
        });
        carouselView.setPageCount(attraction.getExtraWideImages().size() + 1);
    }
}
