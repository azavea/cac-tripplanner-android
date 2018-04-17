package com.gophillygo.app.data.models;

import android.arch.persistence.room.Embedded;

public class EventInfo extends AttractionInfo<Event> {
    @Embedded
    private final Event event;

    // fetch name of related destination from database into this property
    private final String destinationName;

    public EventInfo(Event event, String destinationName, AttractionFlag.Option option) {
        super(event, option);
        this.event = event;
        this.destinationName = destinationName;
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
}
