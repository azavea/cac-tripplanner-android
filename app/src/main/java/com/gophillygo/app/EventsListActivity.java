package com.gophillygo.app;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gophillygo.app.adapters.EventsListAdapter;
import com.gophillygo.app.data.EventViewModel;
import com.gophillygo.app.data.models.AttractionInfo;
import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.models.EventInfo;
import com.gophillygo.app.data.networkresource.Resource;
import com.gophillygo.app.data.networkresource.Status;
import com.gophillygo.app.di.GpgViewModelFactory;

import java.util.List;

import javax.inject.Inject;

public class EventsListActivity extends FilterableListActivity
        implements EventsListAdapter.AttractionListItemClickListener {

    private static final String LOG_LABEL = "EventsList";

    private LinearLayoutManager layoutManager;
    private RecyclerView eventsListView;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    EventViewModel viewModel;

    public EventsListActivity() {
        super(R.layout.activity_events_list, R.id.events_list_toolbar, R.id.events_list_filter_button);
    }

    /**
     * TODO: #20
     * Go to event detail view when an event in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedAttraction(int position) {
        // Get database ID for event clicked, based on positional offset, and pass it along
        long eventId = eventsListView.getAdapter().getItemId(position);
        Log.d(LOG_LABEL, "Clicked event with ID: " + eventId);
        /*
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EVENT_ID_KEY, eventId);
        startActivity(intent);
        */
    }

    public boolean clickedFlagOption(MenuItem item, AttractionInfo eventInfo, Integer position) {
        eventInfo.updateAttractionFlag(item.getItemId());
        viewModel.updateAttractionFlag(eventInfo.getFlag());
        eventsListView.getAdapter().notifyItemChanged(position);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutManager = new LinearLayoutManager(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EventViewModel.class);
        LiveData<Resource<List<EventInfo>>> data = viewModel.getEvents();
        data.observe(this, destinationResource -> {
            if (destinationResource != null && destinationResource.status.equals(Status.SUCCESS) &&
                    destinationResource.data != null && !destinationResource.data.isEmpty()) {

                eventsListView = findViewById(R.id.events_list_recycler_view);
                EventsListAdapter adapter = new EventsListAdapter(this, destinationResource.data, this);
                eventsListView.setAdapter(adapter);
                eventsListView.setLayoutManager(layoutManager);
                data.removeObservers(this);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_event_place:
                Log.d(LOG_LABEL, "Selected event place menu item");
                break;
            case R.id.action_event_map:
                Log.d(LOG_LABEL, "Selected map menu item");
                break;
            case R.id.action_event_search:
                Log.d(LOG_LABEL, "Selected search menu item");
                break;
            default:
                Log.w(LOG_LABEL, "Unrecognized menu item selected: " + String.valueOf(itemId));
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
