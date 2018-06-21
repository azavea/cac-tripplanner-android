package org.gophillygo.app;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;

import com.synnapps.carouselview.ViewListener;

import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.databinding.CustomCarouselItemBinding;

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
        // get destination and set up data binding
        Destination destination = getDestinationAt(position);
        // root here must be null (not the carouselView) to avoid ViewPager stack overflow
        CustomCarouselItemBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_carousel_item,
                null,
                false);
        binding.setDestination(destination);
        binding.setShowNearbyLabel(showNearbyLabel);
        return binding.getRoot();
    }
}
