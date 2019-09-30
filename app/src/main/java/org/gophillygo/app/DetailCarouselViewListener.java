package org.gophillygo.app;

import android.app.Activity;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;

import com.synnapps.carouselview.ViewListener;

import org.gophillygo.app.databinding.CustomDetailCarouselItemBinding;

/**
 * Set up image carousel with destination images and scrim.
 */

public abstract class DetailCarouselViewListener implements ViewListener {

    private final LayoutInflater inflater;

    public DetailCarouselViewListener(Activity activity) {
        this.inflater = activity.getLayoutInflater();
    }

    /**
     * Get the image URL to use for the carousel at the given position.
     * @param position Scroll offset of the carousel
     * @return image URL for the given position
     */
    public abstract String getImageUrlAt(int position);

    @Override
    public View setViewForPosition(int position) {
        // get image URL and set up data binding
        String imageUrl = getImageUrlAt(position);
        // root here must be null (not the carouselView) to avoid ViewPager stack overflow
        CustomDetailCarouselItemBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.custom_detail_carousel_item,
                null,
                false);
        binding.setImageUrl(imageUrl);
        return binding.getRoot();
    }
}
