package com.gophillygo.app;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gophillygo.app.adapters.PlacesListAdapter;
import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.DestinationInfo;
import com.gophillygo.app.data.models.Filter;
import com.gophillygo.app.data.networkresource.Resource;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;

import java.util.List;

import javax.inject.Inject;


public class PlacesListActivity extends FilterableListActivity implements
        PlacesListAdapter.AttractionListItemClickListener {

    private static final String LOG_LABEL = "PlacesList";

    private LinearLayoutManager layoutManager;
    private RecyclerView placesListView;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    public PlacesListActivity() {
        super(R.layout.activity_places_list, R.id.places_list_toolbar, R.id.places_list_filter_button);
    }

    /**
     * Go to place detail view when a place in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedAttraction(int position) {
        // Get database ID for place clicked, based on positional offset, and pass it along
        long destinationId = placesListView.getAdapter().getItemId(position);
        Intent intent = new Intent(this, PlaceDetailActivity.class);
        intent.putExtra(PlaceDetailActivity.DESTINATION_ID_KEY, destinationId);
        startActivity(intent);
    }

    public boolean clickedFlagOption(MenuItem item, AttractionInfo destinationInfo, Integer position) {
        destinationInfo.updateAttractionFlag(item.getItemId());
        viewModel.updateAttractionFlag(destinationInfo.getFlag());
        placesListView.getAdapter().notifyItemChanged(position);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up list of places
        layoutManager = new LinearLayoutManager(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        loadData(null);
    }

    @Override
    protected void loadData(Filter filter) {
        LiveData<Resource<List<DestinationInfo>>> data = viewModel.getFilteredDestinations(filter);
        data.observe(this, destinationResource -> {
            if (destinationResource != null && destinationResource.status.equals(Status.SUCCESS) &&
                    destinationResource.data != null) {
                TextView noDataView = findViewById(R.id.empty_places_list);
                noDataView.setVisibility(destinationResource.data.isEmpty() ? View.VISIBLE : View.GONE);

                placesListView = findViewById(R.id.places_list_recycler_view);
                PlacesListAdapter adapter = new PlacesListAdapter(this, destinationResource.data, this);
                placesListView.setAdapter(adapter);
                placesListView.setLayoutManager(layoutManager);
                // Remove observer after loading full list so updates to the destination flags don't
                // cause unwanted changes to scroll position
                data.removeObservers(this);
            }
        });
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

}
