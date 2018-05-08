package com.gophillygo.app.data.models;

import android.arch.persistence.room.Embedded;

import java.util.ArrayList;
import java.util.Objects;

public class EventInfo extends AttractionInfo<Event> {
    @Embedded
    private final Event event;

    // fetch fields of related destination from database into these properties
    private final String destinationName;
    private final ArrayList<String> destinationCategories;

    @Embedded
    private final DestinationLocation location;
    private Float distance;

    public EventInfo(Event event, AttractionFlag.Option option, String destinationName,
                     ArrayList<String> destinationCategories, Float distance, DestinationLocation location) {
        super(event, option);
        this.event = event;
        this.destinationName = destinationName;
        this.destinationCategories = destinationCategories;
        this.distance = distance;
        this.location = location;
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

    public Float getDistance() {
        return distance;
    }

    public DestinationLocation getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EventInfo eventInfo = (EventInfo) o;
        return Objects.equals(event, eventInfo.event) &&
                Objects.equals(destinationName, eventInfo.destinationName) &&
                Objects.equals(destinationCategories, eventInfo.destinationCategories) &&
                Objects.equals(distance, eventInfo.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), event, destinationName, destinationCategories, distance);
    }
}
