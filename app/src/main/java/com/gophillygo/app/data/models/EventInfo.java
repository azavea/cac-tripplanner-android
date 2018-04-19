package com.gophillygo.app.data.models;

import android.arch.persistence.room.Embedded;

import java.util.ArrayList;

public class EventInfo extends AttractionInfo<Event> {
    @Embedded
    private final Event event;

    // fetch fields of related destination from database into these properties
    private final String destinationName;
    private final ArrayList<String> destinationCategories;

    public EventInfo(Event event, String destinationName, ArrayList<String> destinationCategories,
                     AttractionFlag.Option option) {
        super(event, option);
        this.event = event;
        this.destinationName = destinationName;
        this.destinationCategories = destinationCategories;
    }

    @Override
    public Event getAttraction() {
        return event;
    }

    public Event getEvent() {
        return event;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public boolean hasDestinationName() {
        return destinationName != null && !destinationName.isEmpty();
    }

    public ArrayList<String> getDestinationCategories() {
        return destinationCategories;
    }
}
