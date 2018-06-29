package org.gophillygo.app.activities;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.synnapps.carouselview.CarouselView;

import org.gophillygo.app.CarouselViewListener;
import org.gophillygo.app.R;
import org.gophillygo.app.adapters.PlaceCategoryGridAdapter;
import org.gophillygo.app.data.DestinationRepository;
import org.gophillygo.app.data.models.CategoryAttraction;
import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.gophillygo.app.activities.FilterableListActivity.FILTER_KEY;
import static org.gophillygo.app.activities.PlaceDetailActivity.DESTINATION_ID_KEY;


public class HomeActivity extends BaseAttractionActivity implements DestinationRepository.CategoryAttractionCallback,
PlaceCategoryGridAdapter.GridViewHolder.PlaceGridItemClickListener {

    private static final String LOG_LABEL = "HomeActivity";

    // how many columns to display in grid of filter buttons
    private static final int NUM_COLUMNS = 2;
    private static final int IMAGE_SIZE = 400;

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
        addScrollListener();

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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_LABEL, "onResume");
        addScrollListener();
    }

    private void addScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerViewPreloader<>(Glide.with(this),
                new ListPreloader.PreloadModelProvider<CategoryAttraction>() {
                    @NonNull
                    @Override
                    public List<CategoryAttraction> getPreloadItems(int position) {
                        return Collections.singletonList(categories.get(position));
                    }

                    @Nullable
                    @Override
                    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull CategoryAttraction item) {
                        RequestOptions options = new RequestOptions().centerCrop().override(IMAGE_SIZE, IMAGE_SIZE).encodeQuality(100);
                        return Glide.with(HomeActivity.this).load(item.getImage()).apply(options);
                    }
                }, new FixedPreloadSizeProvider<>(IMAGE_SIZE, IMAGE_SIZE), categories.size()));
    }

    private void setUpCarousel() {
        Log.d(LOG_LABEL, "set up carousel with size: " + getNearestDestinationSize());

        carouselView.pauseCarousel();
        carouselView.setViewListener(new CarouselViewListener(this) {
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

        // Go to place detail view on carousel interaction
        carouselView.setImageClickListener(position -> {
            Log.d(LOG_LABEL, "Clicked item at " + position);
            Destination destination = getNearestDestination(position);
            if (destination != null) {
                goToPlace((long)destination.getId());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        setupSearch(menu, R.id.action_home_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_place_list_search:
                Log.d(LOG_LABEL, "Clicked search action");
                break;
            case R.id.action_settings:
                Log.d(LOG_LABEL, "Clicked settings action");
                Intent intent = new Intent(this, GpgPreferenceActivity.class);
                startActivity(intent);
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
                if (categories == null || position >= categories.size()) {
                    Log.e(LOG_LABEL, "Cannot go to filtered list because categories are missing");
                    startActivity(new Intent(this, PlacesListActivity.class));
                }
                CategoryAttraction attraction = categories.get(position);
                goToFilteredPlacesList(attraction.getCategory());
        }
    }

    private void goToFilteredPlacesList(CategoryAttraction.PlaceCategories category) {
        Filter filter = new Filter();
        switch (category) {
            case WantToGo:
                filter.setWantToGo(true);
                break;
            case Liked:
                filter.setLiked(true);
                break;
            case Nature:
                filter.setNature(true);
                break;
            case Exercise:
                filter.setExercise(true);
                break;
            case Educational:
                filter.setEducational(true);
                break;
            default:
                Log.e(LOG_LABEL, "Unrecognized place category " + category.displayName);
        }
        Intent intent = new Intent(this, PlacesListActivity.class);
        intent.putExtra(FILTER_KEY, filter);
        startActivity(intent);
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
