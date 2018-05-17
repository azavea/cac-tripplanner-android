package com.gophillygo.app.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gophillygo.app.R;
import com.gophillygo.app.adapters.PlacesListAdapter;
import com.gophillygo.app.data.models.AttractionFlag;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.databinding.ActivityPlacesListBinding;
import com.gophillygo.app.databinding.FilterButtonBarBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlacesListActivity extends FilterableListActivity implements
        PlacesListAdapter.AttractionListItemClickListener {

    private static final String LOG_LABEL = "PlacesList";
    private static final int RETURN_FROM_DETAIL_REQUEST = 301;

    private LinearLayoutManager layoutManager;
    private RecyclerView placesListView;
    PlacesListAdapter placesListAdapter;

    // Track the ID of the destination last picked to view its details, so we can update it
    // on return to this list, without having to reset the full list.
    private long detailDestinationId = -1;

    public PlacesListActivity() {
        super(R.id.places_list_toolbar);

        // If destinations were loaded before this activity showed, use them immediately.
        if (destinationInfos != null && !destinationInfos.isEmpty()) {
            loadData();
        } else {
            Log.d(LOG_LABEL, "Have no destinations for the places list");
        }
    }

    @Override
    public void locationOrDestinationsChanged() {
        super.locationOrDestinationsChanged();
        if (destinationInfos != null && !destinationInfos.isEmpty()) {
            loadData();
        } else {
            Log.d(LOG_LABEL, "Have no destinations for the places list in locationOrDestinationsChanged");
        }
    }

    /**
     * Go to place detail view when a place in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedAttraction(int position) {
        // Get database ID for place clicked, based on positional offset, and pass it along
        detailDestinationId = placesListView.getAdapter().getItemId(position);
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.DESTINATION_ID_KEY, detailDestinationId);
        startActivityForResult(intent, RETURN_FROM_DETAIL_REQUEST);
    }

    public boolean clickedFlagOption(MenuItem item, AttractionInfo destinationInfo, Integer position) {
        AttractionFlag lastFlag = destinationInfo.getFlag();
        destinationInfo.updateAttractionFlag(item.getItemId());
        AttractionFlag flag = destinationInfo.getFlag();
        viewModel.updateAttractionFlag(flag);

        if (!Objects.equals(flag, lastFlag)) {
            // Handle item disappearing from filtered list when filter condition changed.
            if (filter.count() > 0 && filter.matches((DestinationInfo)destinationInfo)) {
                placesListAdapter.notifyItemRemoved(position);
            } else {
                placesListAdapter.notifyItemChanged(position);
            }
        } else {
            Log.d(LOG_LABEL, "Ignoring option being set to current value.");
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up list of places
        layoutManager = new LinearLayoutManager(this);
        placesListView = findViewById(R.id.places_list_recycler_view);
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        ActivityPlacesListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_places_list);
        return binding.placesListFilterButtonBar;
    }

    @Override
    protected void loadData() {
        Log.d(LOG_LABEL, "loadData");
        List<DestinationInfo> filteredDestinations = getFilteredDestinations();
        TextView noDataView = findViewById(R.id.empty_places_list);
        noDataView.setVisibility(filteredDestinations.isEmpty() ? View.VISIBLE : View.GONE);

        // Reset list adapter if either it isn't set up, or if a filter was applied/removed.
        if (placesListAdapter == null || filteredDestinations.size() != placesListAdapter.getItemCount()) {
            placesListAdapter = new PlacesListAdapter(this, filteredDestinations, this);
            // must set the list before the adapter for the differ to initialize properly
            placesListAdapter.submitList(filteredDestinations);
            placesListView.setAdapter(placesListAdapter);
            placesListView.setLayoutManager(layoutManager);
        } else {
            Log.d(LOG_LABEL, "submit list for diff");
            // Let the AsyncListDiffer find which have changed, and only update their view holders
            // https://developer.android.com/reference/android/support/v7/recyclerview/extensions/ListAdapter
            placesListAdapter.submitList(filteredDestinations);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.places_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_events:
                Log.d(LOG_LABEL, "Selected events menu item");
                break;
            case R.id.action_map:
                Log.d(LOG_LABEL, "Selected map menu item");
                startActivity(new Intent(this, MapsActivity.class));
                break;
            case R.id.action_search:
                Log.d(LOG_LABEL, "Selected search menu item");
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu item selected: " + String.valueOf(itemId));
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @NonNull
    private ArrayList<DestinationInfo> getFilteredDestinations() {
        ArrayList<DestinationInfo> filteredDestinations = new ArrayList<>(destinationInfos.size());
        for (DestinationInfo info : destinationInfos) {
            if (filter.matches(info)) {
                filteredDestinations.add(info);
            }
        }
        return filteredDestinations;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Update on returning from detail view for just the viewed destination.
        // Allows keeping current list with scroll position, while refreshing the viewed
        // destination, in case the user changed its flag.
        if (requestCode == RETURN_FROM_DETAIL_REQUEST) {
            Log.d(LOG_LABEL, "Returned to places list from place detail");
            // update the single list item that may have changed
            if (detailDestinationId > -1) {
                final long destinationId = detailDestinationId;
                detailDestinationId = -1; // reset now it has been handled
                Log.d(LOG_LABEL, "Update viewed list item with ID " + destinationId);
                for (int i = 0; i < destinationInfos.size(); i++) {
                    Destination destination = destinationInfos.get(i).getDestination();
                    if (destination.getId() == destinationId) {
                        final int idx = i;
                        viewModel.getDestination(destinationId).observe(this, (final DestinationInfo destinationInfo) -> {
                            if (destinationInfo == null || destinationInfo.getDestination() == null) {
                                Log.w(LOG_LABEL, "No matching destination found for ID " + destinationId);
                                return;
                            }

                            Log.d(LOG_LABEL, "Found list index for place " + idx);
                            destinationInfos.set(idx, destinationInfo);
                            placesListAdapter.submitList(getFilteredDestinations());
                        });
                        break;
                    }
                }
            }
        } else {
            Log.w(LOG_LABEL, "Unrecognized requestCode " + requestCode);
        }
    }
}
