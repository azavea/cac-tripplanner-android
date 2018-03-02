package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.gophillygo.app.data.models.Destination;
import com.gophillygo.app.data.networkresource.Resource;

import java.util.List;

import javax.inject.Inject;


public class DestinationViewModel extends ViewModel {
    private final LiveData<Resource<List<Destination>>> destinations;
    private final DestinationRepository destinationRepository;

    @Inject
    public DestinationViewModel(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
        destinations = destinationRepository.loadDestinations();
    }

    public LiveData<Destination> getDestination(String destinationId) {
        return destinationRepository.getDestination(destinationId);
    }

    public void updateDestination(Destination destination) {
        destinationRepository.updateDestination(destination);
    }

    public void updateMultipleDestinations(List<Destination> destinations) {
        destinationRepository.updateMultipleDestinations(destinations);
    }

    public LiveData<Resource<List<Destination>>> getDestinations() {
        return destinations;
    }
}
