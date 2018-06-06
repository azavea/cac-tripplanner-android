package com.gophillygo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import com.gophillygo.app.CarouselViewListener;
import com.gophillygo.app.R;
import com.gophillygo.app.adapters.PlaceCategoryGridAdapter;
import com.gophillygo.app.data.models.AttractionFlag;
import com.gophillygo.app.data.models.Destination;
import com.synnapps.carouselview.CarouselView;

import java.util.List;


public class HomeActivity extends BaseAttractionActivity {

    private static final String LOG_LABEL = "HomeActivity";

    private CarouselView carouselView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(LOG_LABEL, "onCreate");

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        // disable default app name title display
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        GridView gridView = findViewById(R.id.home_grid_view);
        gridView.setAdapter(new PlaceCategoryGridAdapter(this));
        gridView.setOnItemClickListener((parent, v, position, id) -> clickedGridItem(position));

        carouselView = findViewById(R.id.home_carousel);
        carouselView.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        carouselView.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        // initialize carousel if destinations already loaded
        locationOrDestinationsChanged();

    }

    @Override
    public void locationOrDestinationsChanged() {
        Log.d(LOG_LABEL, "Location or destinations changed; setting up carousel");
        // set up carousel with nearest destinations
        if (getNearestDestinationSize() > 0) {
            setUpCarousel();
        } else {
            Log.w(LOG_LABEL, "No nearest destinations yet in locationOrDestinationChanged");
        }
    }

    private void setUpCarousel() {
        Log.d(LOG_LABEL, "set up carousel with size: " + getNearestDestinationSize());

        carouselView.pauseCarousel();
        carouselView.setViewListener(new CarouselViewListener(this, true) {
            @Override
            public Destination getDestinationAt(int position) {
                return getNearestDestination(position);
            }
        });
        carouselView.setPageCount(getNearestDestinationSize());

        // prompt carousel to refresh currently displayed item
        if (getNearestDestinationSize() > 0) {
            carouselView.setCurrentItem(0);
            carouselView.playCarousel();
        } else {
            Log.d(LOG_LABEL, "Nearest destinations collection empty; nothing to put in carousel.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_search:
                Log.d(LOG_LABEL, "Clicked search action");
                break;
            case R.id.action_settings:
                Log.d(LOG_LABEL, "Clicked settings action");
                break;
            case R.id.action_about:
                Log.d(LOG_LABEL, "Clicked about action");
                break;
            case R.id.action_logout:
                Log.d(LOG_LABEL, "Clicked logout action");
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu option selected: " + itemId);
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onPause() {
        carouselView.pauseCarousel();
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        carouselView.playCarousel();
    }

    private void clickedGridItem(int position) {
        Log.d(LOG_LABEL, "clicked grid view item: " + position);

        switch (position) {
            case 0:
                // go to events list
                startActivity(new Intent(this, EventsListActivity.class));
                break;
            default:
                // go to places list
                // TODO: #18 filter list based on selected grid item
                startActivity(new Intent(this, PlacesListActivity.class));
        }
    }
}
