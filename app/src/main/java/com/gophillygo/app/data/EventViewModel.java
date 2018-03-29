package com.gophillygo.app.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.gophillygo.app.data.models.Event;
import com.gophillygo.app.data.networkresource.Resource;

import java.util.List;

import javax.inject.Inject;


public class EventViewModel extends ViewModel {

    private final LiveData<Resource<List<Event>>> events;
    private final DestinationRepository destinationRepository;

    @Inject
    public EventViewModel(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
        events = destinationRepository.loadEvents();
    }

    public LiveData<Event> getEvent(long eventId) {
        return destinationRepository.getEvent(eventId);
    }

    public void updateEvent(Event event) {
        destinationRepository.updateEvent(event);
    }

    public void updateMultipleEvents(List<Event> events) {
        destinationRepository.updateMultipleEvents(events);
    }

    public LiveData<Resource<List<Event>>> getEvents() {
        return events;
    }
}
