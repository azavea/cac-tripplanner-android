package org.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.database.Cursor;

import org.gophillygo.app.data.models.Destination;
import org.gophillygo.app.data.models.DestinationInfo;
import org.gophillygo.app.data.networkresource.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class DestinationViewModel extends AttractionViewModel {
    private final LiveData<Resource<List<DestinationInfo>>> destinations;

    @Inject
    public DestinationViewModel(DestinationRepository destinationRepository) {
        super(destinationRepository);
        destinations = destinationRepository.loadDestinations();
    }

    public LiveData<DestinationInfo> getDestination(long destinationId) {
        return destinationRepository.getDestination(destinationId);
    }

    public Cursor searchAttractions(String search) {
        return destinationRepository.searchAttractions(search);
    }

    public void updateDestination(Destination destination) {
        destinationRepository.updateDestination(destination);
    }

    public void updateMultipleDestinations(List<DestinationInfo> infos) {
        List<Destination> destinations = new ArrayList<>(infos.size());
        for (DestinationInfo info: infos) {
            destinations.add(info.getDestination());
        }
        // update destinations all at once, so LiveData observer only triggers once
        destinationRepository.updateMultipleDestinations(destinations);
    }

    public LiveData<Resource<List<DestinationInfo>>> getDestinations() {
        return destinations;
    }

    public void getCategoryAttractions(DestinationRepository.CategoryAttractionCallback listener) {
        destinationRepository.loadCategoryAttractions(listener);
    }
}
