package com.gophillygo.app.activities;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.gophillygo.app.CarouselViewListener;
import com.gophillygo.app.R;
import com.gophillygo.app.adapters.PlaceCategoryGridAdapter;
import com.gophillygo.app.data.DestinationRepository;
import com.gophillygo.app.data.models.CategoryAttraction;
import com.gophillygo.app.data.models.Destination;
import com.synnapps.carouselview.CarouselView;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends BaseAttractionActivity implements DestinationRepository.CategoryAttractionCallback,
PlaceCategoryGridAdapter.GridViewHolder.PlaceGridItemClickListener {

    private static final String LOG_LABEL = "HomeActivity";

    // how many columns to display in grid of filter buttons
    private static final int NUM_COLUMNS = 2;

    private CarouselView carouselView;
    RecyclerView recyclerView;
    PlaceCategoryGridAdapter gridAdapter;
    List<CategoryAttraction> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(LOG_LABEL, "onCreate");
        categories = new ArrayList<>(CategoryAttraction.PlaceCategories.size());

        Toolbar toolbar = findViewById(R.id.home_toolbar);
        // disable default app name title display
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.home_grid_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, NUM_COLUMNS);
        recyclerView.setLayoutManager(layoutManager);
        gridAdapter = new PlaceCategoryGridAdapter(this, this);
        recyclerView.setAdapter(gridAdapter);

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
            // request random images for the filter grid categories, and notify the adapter
            viewModel.getCategoryAttractions(this);
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

    @Override
    public void clickedGridItem(int position) {
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

    @Override
    public void gotCategoryAttractions(LiveData<List<CategoryAttraction>> categoryAttractions) {
        Log.d(LOG_LABEL, "Getting category attractions");
        categoryAttractions.observe(this, data -> {
            recyclerView.post(() -> {
                Log.d(LOG_LABEL, "Got category attractions");
                if (data == null || data.isEmpty()) {
                    Log.e(LOG_LABEL, "Category attractions are missing");
                    return;
                }

                // Submit `categories` list managed by this activity, rather than the `data` list
                // owned by the DAO, in order to be able to remove categories with no entries
                // (empty image). Do not destroy/recreate list here, so list differ will
                // correctly recognize that the reference hasn't changed.

                categories.clear();
                categories.addAll(data);
                gridAdapter.submitList(categories);
                gridAdapter.notifyDataSetChanged();

                for (CategoryAttraction attraction: data) {
                    if (attraction.getImage().isEmpty()) {
                        categories.remove(attraction);
                        gridAdapter.submitList(categories);
                        gridAdapter.notifyItemRemoved(attraction.getCategory().code);
                    }
                }
            });
        });
    }
}
