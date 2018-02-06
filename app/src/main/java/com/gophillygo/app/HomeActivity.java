package com.gophillygo.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

public class HomeActivity extends AppCompatActivity {

    CarouselView carouselView;
    LayoutInflater inflater;


    // order corresponds to the image URLs below
    String[] testPlaceNames = {
            "Camden County Environmental Education Center",
            "Bartram's Garden",
            "Cobb's Creek Trail"
    };

    // destination wide images, 680x400
    String[] testImageUrls = {
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/e6aa6bc0891247c4a4d651f22c028fe6.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/874f2bd93b5f4bc692cf39d1aaba5ead.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/ad72d3d20dfb4197b76c7b4d211a8eef.jpg"

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        inflater = getLayoutInflater();

        carouselView = findViewById(R.id.home_carousel);
        carouselView.setPageCount(testPlaceNames.length);
        carouselView.setSlideInterval(4000);
        carouselView.setViewListener(viewListener);
    }

    ViewListener viewListener = new ViewListener() {
        @Override
        public View setViewForPosition(int position) {
            View itemView = inflater.inflate(R.layout.custom_carousel_item, null);
            ImageView carouselImageView = itemView.findViewById(R.id.carousel_item_image);
            TextView carouselPlaceName = itemView.findViewById(R.id.carousel_item_place_name);

            Glide.with(HomeActivity.this)
                    .load(testImageUrls[position])
                    .into(carouselImageView);

            carouselPlaceName.setText(testPlaceNames[position]);
            carouselImageView.setContentDescription(testPlaceNames[position]);
            carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
            return itemView;
        }
    };
}
