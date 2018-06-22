package org.gophillygo.app.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
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

import org.gophillygo.app.R;
import org.gophillygo.app.adapters.EventsListAdapter;
import org.gophillygo.app.data.EventViewModel;
import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.AttractionInfo;
import org.gophillygo.app.data.models.EventInfo;
import org.gophillygo.app.data.networkresource.Resource;
import org.gophillygo.app.data.networkresource.Status;
import org.gophillygo.app.databinding.ActivityEventsListBinding;
import org.gophillygo.app.databinding.FilterButtonBarBinding;
import org.gophillygo.app.di.GpgViewModelFactory;
import org.gophillygo.app.tasks.AddGeofencesBroadcastReceiver;
import org.gophillygo.app.tasks.RemoveGeofenceWorker;

import org.gophillygo.app.data.models.EventInfo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class EventsListActivity extends FilterableListActivity
        implements EventsListAdapter.AttractionListItemClickListener {

    private static final String LOG_LABEL = "EventsList";

    private LinearLayoutManager layoutManager;
    private RecyclerView eventsListView;
    private List<EventInfo> events;
    private EventsListAdapter adapter;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    EventViewModel viewModel;

    public EventsListActivity() {
        super(R.id.events_list_toolbar);
    }

    /**
     * Go to event detail view when an event in the list clicked.
     *
     * @param position Offset of the position of the list item clicked
     */
    public void clickedAttraction(int position) {
        // Get database ID for event clicked, based on positional offset, and pass it along
        long eventId = eventsListView.getAdapter().getItemId(position);
        Log.d(LOG_LABEL, "Clicked event with ID: " + eventId);
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EVENT_ID_KEY, eventId);
        startActivity(intent);
    }

    public boolean clickedFlagOption(MenuItem item, AttractionInfo eventInfo, Integer position) {
        Boolean haveExistingGeofence = eventInfo.getFlag()
                .getOption().api_name.equals(AttractionFlag.Option.WantToGo.api_name);

        eventInfo.updateAttractionFlag(item.getItemId());
        viewModel.updateAttractionFlag(eventInfo.getFlag(), userUuid, getString(R.string.user_flag_post_api_key));
        adapter.notifyItemChanged(position);

        // do not attempt to add a geofence for an event with no location
        if (((EventInfo)eventInfo).hasDestinationName()) {
            Boolean settingGeofence = eventInfo.getFlag().getOption().api_name.equals(AttractionFlag.Option.WantToGo.api_name);
            addOrRemoveGeofence(eventInfo, haveExistingGeofence, settingGeofence);
        } else {
            // TODO: notify user?
            Log.w(LOG_LABEL, "Cannot add geofence for an event without an associated destination");
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutManager = new LinearLayoutManager(this);
        eventsListView = findViewById(R.id.events_list_recycler_view);

        // In addition to the destination data loaded by the BaseAttraction, get the full
        // events data here.
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EventViewModel.class);
        LiveData<Resource<List<EventInfo>>> data = viewModel.getEvents();
        data.observe(this, destinationResource -> {
            if (destinationResource != null && destinationResource.status.equals(Status.SUCCESS) &&
                    destinationResource.data != null && !destinationResource.data.isEmpty()) {
                events = destinationResource.data;
                loadData();
            }
        });
    }

    @Override
    protected FilterButtonBarBinding setupDataBinding() {
        ActivityEventsListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_events_list);
        return binding.eventsListFilterButtonBar;
    }

    @Override
    protected void loadData() {
        if (eventsListView == null) return;
        List<EventInfo> filteredEvents = getFilteredEvents();

        TextView noDataView = findViewById(R.id.empty_events_list);
        noDataView.setVisibility(filteredEvents.isEmpty() ? View.VISIBLE : View.GONE);

        // Reset list adapter if either it isn't set up, or if a filter was applied/removed.
        if (adapter == null || filteredEvents.size() != adapter.getItemCount()) {
            adapter = new EventsListAdapter(this, filteredEvents, this);
            adapter.submitList(filteredEvents);
            eventsListView.setAdapter(adapter);
            eventsListView.setLayoutManager(layoutManager);
        } else {
            // Let the AsyncListDiffer find which have changed, and only update their view holders
            adapter.submitList(filteredEvents);
        }
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
                startActivity(new Intent(this, EventsMapsActivity.class));
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

    @NonNull
    private List<EventInfo> getFilteredEvents() {
        if (events == null) return new ArrayList<>(0);
        List<EventInfo> filteredEvents = new ArrayList<>(events.size());
        for (EventInfo info : events) {
            if (filter.matches(info)) {
                filteredEvents.add(info);
            }
        }
        return filteredEvents;
    }
}