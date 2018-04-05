package com.gophillygo.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.app.data.models.Destination;
import com.synnapps.carouselview.ViewListener;

/**
 * Set up image carousel with destination image and related information overlaid in text views.
 */

public abstract class CarouselViewListener implements ViewListener {

    private final LayoutInflater inflater;
    private final Activity activity;
    private final boolean showNearbyLabel;

    public CarouselViewListener(Activity activity, boolean showNearbyLabel) {
        this.activity = activity;
        this.inflater = this.activity.getLayoutInflater();
        this.showNearbyLabel = showNearbyLabel;
    }

    /**
     * Get the destination to use as the data source for the carousel at the given position.
     * @param position Scroll offset of the carousel
     * @return Destination to display at that position
     */
    public abstract Destination getDestinationAt(int position);

    @Override
    public View setViewForPosition(int position) {
        // root here must be null (not the carouselView) to avoid ViewPager stack overflow
        @SuppressLint("InflateParams") View itemView = inflater.inflate(R.layout.custom_carousel_item, null);
        ImageView carouselImageView = itemView.findViewById(R.id.carousel_item_image);
        TextView carouselPlaceName = itemView.findViewById(R.id.carousel_item_place_name);
        TextView carouselDistance = itemView.findViewById(R.id.carousel_item_distance_label);

        Destination destination = getDestinationAt(position);

        if (!showNearbyLabel) {
            TextView carouselNearby = itemView.findViewById(R.id.carousel_item_nearby_label);
            carouselNearby.setVisibility(View.GONE);
        }

        Glide.with(this.activity)
                .load(destination.getWideImage())
                .into(carouselImageView);

        carouselPlaceName.setText(destination.getAddress());
        carouselImageView.setContentDescription(destination.getAddress());
        carouselDistance.setText(destination.getFormattedDistance());

        return itemView;
    }
}
