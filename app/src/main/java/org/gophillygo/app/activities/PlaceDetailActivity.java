package org.gophillygo.app.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import org.gophillygo.app.BR;
import org.gophillygo.app.R;
import org.gophillygo.app.data.DestinationViewModel;
import org.gophillygo.app.data.models.AttractionFlag;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.databinding.ActivityPlaceDetailBinding;
import org.gophillygo.app.di.GpgViewModelFactory;
import org.gophillygo.app.utils.FlagMenuUtils;

import org.gophillygo.app.tasks.GeofenceTransitionWorker;

import javax.inject.Inject;

import static org.gophillygo.app.tasks.GeofenceTransitionWorker.MARK_BEEN_KEY;

public class PlaceDetailActivity extends AttractionDetailActivity {

    public static final String DESTINATION_ID_KEY = "place_id";
    private static final String LOG_LABEL = "PlaceDetail";

    private long placeId = -1;
    private ActivityPlaceDetailBinding binding;

    @SuppressWarnings("WeakerAccess")
    @Inject
    GpgViewModelFactory viewModelFactory;
    @SuppressWarnings("WeakerAccess")
    DestinationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_place_detail);
        binding.setActivity(this);

        // disable default app name title display
        binding.placeDetailToolbar.setTitle("");
        setSupportActionBar(binding.placeDetailToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(DESTINATION_ID_KEY)) {
            placeId = getIntent().getLongExtra(DESTINATION_ID_KEY, -1);
        }

        if (placeId == -1) {
            Log.e(LOG_LABEL, "Place not found when attempting to load detail view.");
            finish();
        }

        binding.placeDetailCarousel.setIndicatorGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
        binding.placeDetailCarousel.setImageClickListener(position ->
                Log.d(LOG_LABEL, "Clicked item: "+ position));

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DestinationViewModel.class);
        LiveData<DestinationInfo> data = viewModel.getDestination(placeId);

        data.observe(this, destinationInfo -> {
            // TODO: #61 handle if destination not found (go to list of destinations?)
            if (destinationInfo == null || destinationInfo.getDestination() == null) {
                String message = "No matching destination found for ID " + placeId;
                Log.e(LOG_LABEL, message);
                Crashlytics.log(message);
                return;
            }

            this.destinationInfo = destinationInfo;

            // Check if flag set by notification to mark event as "been" and set flag if so
            if (getIntent().hasExtra(GeofenceTransitionWorker.MARK_BEEN_KEY)) {
                if(getIntent().getBooleanExtra(GeofenceTransitionWorker.MARK_BEEN_KEY, false) &&
                        !destinationInfo.getFlag().getOption().id.equals(AttractionFlag.Option.Been.id)) {

                    updateFlag(AttractionFlag.Option.Been.id);
                }
            }

            // set up data binding object
            binding.setDestination(destinationInfo.getDestination());
            binding.setDestinationInfo(destinationInfo);
            binding.setActivity(this);
            binding.setContext(this);
            binding.placeDetailDescriptionCard.detailDescriptionToggle.setOnClickListener(toggleClickListener);
            displayDestination();
        });
    }

    @SuppressLint({"RestrictedApi", "RtlHardcoded"})
    private void displayDestination() {
        setupCarousel(binding.placeDetailCarousel, destinationInfo.getDestination());
    }

    // show popover for flag options (been, want to go, etc.)
    public void userFlagChanged(View view) {
        Log.d(LOG_LABEL, "Clicked flags button");
        PopupMenu menu = FlagMenuUtils.getFlagPopupMenu(this, view, destinationInfo.getFlag());
        menu.setOnMenuItemClickListener(item -> {
            updateFlag(item.getItemId());
            return true;
        });
    }

    private void updateFlag(int itemId) {
        if (destinationInfo == null) {
            String message = "Cannot update flag because destination is not set";
            Log.e(LOG_LABEL, message);
            Crashlytics.log(message);
            return;
        }
        Boolean haveExistingGeofence = destinationInfo.getFlag().getOption()
                .api_name.equals(AttractionFlag.Option.WantToGo.api_name);
        destinationInfo.updateAttractionFlag(itemId);
        viewModel.updateAttractionFlag(destinationInfo.getFlag(), userUuid, getString(R.string.user_flag_post_api_key));
        Boolean settingGeofence = itemId  == AttractionFlag.Option.WantToGo.code;
        addOrRemoveGeofence(destinationInfo, haveExistingGeofence, settingGeofence);
        binding.notifyPropertyChanged(BR.destinationInfo);
    }

    // TODO: #113 scroll to bottom, where events are listed inline with the view
    public void goToEvents(View view) {
        Log.d(LOG_LABEL, "Clicked events in destination. TODO: #113");
    }

    @Override
    protected Class getMapActivity() {
        return PlacesMapsActivity.class;
    }

    @Override
    protected int getAttractionId() {
        return (int) placeId;
    }
}
