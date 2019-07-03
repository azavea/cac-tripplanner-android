package org.gophillygo.app;

import android.app.Activity;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;

import com.synnapps.carouselview.ViewListener;

import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.databinding.CustomHomeCarouselItemBinding;

/**
 * Set up image carousel with destination image and related information overlaid in text views.
 */

public abstract class HomeCarouselViewListener implements ViewListener {

    private final LayoutInflater inflater;
    private final Activity activity;

    public HomeCarouselViewListener(Activity activity) {
        this.activity = activity;
        this.inflater = this.activity.getLayoutInflater();
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
        CustomHomeCarouselItemBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_home_carousel_item,
                null,
                false);
        binding.setDestination(destination);
        binding.setActivity(activity);
        return binding.getRoot();
    }
}
