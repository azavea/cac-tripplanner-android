package com.gophillygo.app.data.models;

import android.arch.persistence.room.Embedded;

public class DestinationInfo extends AttractionInfo<Destination> {
    @Embedded
    private final Destination destination;

    // These fields come from related tables/aggregates
    private final int eventCount;

    public DestinationInfo(Destination destination, int eventCount, AttractionFlag.Option option) {
        super(destination, option);
        this.destination = destination;
        this.eventCount = eventCount;
    }

    @Override
    public Destination getAttraction() {
        return destination;
    }

    public Destination getDestination() {
        return destination;
    }

    public int getEventCount() {
        return eventCount;
    }

    public boolean hasEvents() {
        return eventCount > 0;
    }

}
