package org.gophillygo.app.activities;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.ViewPreloadSizeProvider;

import org.gophillygo.app.R;
import org.gophillygo.app.adapters.PlacesListAdapter;
import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.databinding.ActivityPlacesListBinding;
import org.gophillygo.app.databinding.FilterButtonBarBinding;
import org.gophillygo.app.utils.UserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlacesListActivity extends FilterableListActivity implements
        PlacesListAdapter.AttractionListItemClickListener, SearchView.OnQueryTextListener {

    private static final String LOG_LABEL = "PlacesList";

    private RecyclerView placesListView;
    PlacesListAdapter placesListAdapter;
    ActivityPlacesListBinding binding;

    public PlacesListActivity() {
        super(R.id.places_list_toolbar);
    }

    @Override
    public void locationOrDestinationsChanged() {
        super.locationOrDestinationsChanged();
        if (destinationInfos != null && !destinationInfos.isEmpty()) {
            loadData();
        } else {
            Log.w(LOG_LABEL, "Have no destinations for the places list in locationOrDestinationsChanged");
        }
    }

    /**
     * Go to place detail view when a place in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedAttraction(int position) {
        // Get database ID for place clicked, based on positional offset, and pass it along
        long detailId = placesListView.getAdapter().getItemId(position);
        goToPlace(detailId);
    }

    public boolean clickedFlagOption(MenuItem item, AttractionInfo destinationInfo, Integer position) {
        String option = destinationInfo.getFlag().getOption().apiName;
        boolean haveExistingGeofence = option.equals(AttractionFlag.Option.WantToGo.apiName) ||
                option.equals(AttractionFlag.Option.Liked.apiName);

        destinationInfo.updateAttractionFlag(item.getItemId());

        viewModel.updateAttractionFlag(destinationInfo.getFlag(),
                userUuid,
                getString(R.string.user_flag_post_api_key),
                UserUtils.isFlagPostingEnabled(this));

        placesListAdapter.notifyItemChanged(position);
        String optionAfter = destinationInfo.getFlag().getOption().apiName;
        boolean settingGeofence = optionAfter.equals(AttractionFlag.Option.WantToGo.apiName) ||
                optionAfter.equals(AttractionFlag.Option.Liked.apiName);
        addOrRemoveGeofence(destinationInfo, haveExistingGeofence, settingGeofence);
	    return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up list of places
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        placesListView = findViewById(R.id.places_list_recycler_view);
        placesListView.setLayoutManager(layoutManager);

        // If destinations were loaded before this activity showed, use them immediately.
        if (destinationInfos != null && !destinationInfos.isEmpty()) {
            loadData();
        } else {
            Log.d(LOG_LABEL, "Have no destinations for the places list");
        }
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_places_list);
        return binding.placesListFilterButtonBar;
    }

    @Override
    protected void loadData() {
        if (placesListView == null) return;
        Log.d(LOG_LABEL, "loadData");
        List<DestinationInfo> filteredDestinations = getFilteredDestinations();

        binding.emptyPlacesList.setVisibility(filteredDestinations.isEmpty() ? View.VISIBLE : View.GONE);

        // Reset list adapter if either it isn't set up, or if a filter was applied/removed.
        if (placesListAdapter == null || filteredDestinations.size() != placesListAdapter.getItemCount()) {
            placesListAdapter = new PlacesListAdapter(this, filteredDestinations, this);
            // must set the list before the adapter for the differ to initialize properly
            placesListAdapter.submitList(filteredDestinations);
            placesListView.setAdapter(placesListAdapter);
            addScrollListener();
        } else {
            Log.d(LOG_LABEL, "submit list for diff");
            // Let the AsyncListDiffer find which have changed, and only update their view holders
            // https://developer.android.com/reference/android/support/v7/recyclerview/extensions/ListAdapter
            placesListAdapter.submitList(filteredDestinations);
        }
        placesListAdapter.notifyDataSetChanged();
        placesListView.requestLayout();
    }

    private void addScrollListener() {

        ImageView imageView = placesListView.findViewById(R.id.place_list_item_image);
        if (imageView == null) {
            Log.e(LOG_LABEL, "image view not found; not setting up scroll listener");
            return;
        }

        placesListView.addOnScrollListener(new RecyclerViewPreloader<>(Glide.with(this),
                new ListPreloader.PreloadModelProvider<DestinationInfo>() {
                    @NonNull
                    @Override
                    public List<DestinationInfo> getPreloadItems(int position) {
                        return Collections.singletonList(destinationInfos.get(position));
                    }

                    @Nullable
                    @Override
                    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull DestinationInfo item) {
                        RequestOptions options = new RequestOptions().centerCrop().encodeQuality(100);
                        return Glide.with(PlacesListActivity.this).load(item.getDestination().getWideImage()).apply(options);
                    }
                }, new ViewPreloadSizeProvider<>(imageView), 6));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.places_list_menu, menu);
        setupSearch(menu, R.id.action_place_list_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        Intent intent;
        switch (itemId) {
            case R.id.action_place_list_events:
                Log.d(LOG_LABEL, "Selected events menu item");
                intent = new Intent(this, EventsListActivity.class);
                intent.putExtra(FILTER_KEY, filter);
                startActivity(intent);
                break;
            case R.id.action_place_list_map:
                Log.d(LOG_LABEL, "Selected map menu item");
                intent = new Intent(this, PlacesMapsActivity.class);
                intent.putExtra(FILTER_KEY, filter);
                startActivity(intent);
                break;
            case R.id.action_place_list_search:
                Log.d(LOG_LABEL, "Selected search menu item");
                super.onSearchRequested();
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu item selected: " + String.valueOf(itemId));
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @NonNull
    private ArrayList<DestinationInfo> getFilteredDestinations() {
        if (destinationInfos == null) {
            return new ArrayList<>(0);
        }
        ArrayList<DestinationInfo> filteredDestinations = new ArrayList<>(destinationInfos.size());
        for (DestinationInfo info : destinationInfos) {
            if (filter.matches(info)) {
                filteredDestinations.add(info);
            }
        }
        return filteredDestinations;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(LOG_LABEL, "onQueryTextSubmit " + query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(LOG_LABEL, "onQueryTextChange " + newText);
        return false;
    }
}
