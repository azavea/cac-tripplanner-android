package com.gophillygo.app.data.models;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

public class EventInfo extends AttractionInfo<Event> {

    private static final NumberFormat numberFormatter = NumberFormat.getNumberInstance();
    static {
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(2);
    }

    @Embedded
    private final Event event;

    // fetch fields of related destination from database into these properties
    private final String destinationName;
    private final ArrayList<String> destinationCategories;

    @Embedded
    private final DestinationLocation location;
    private final Float distance;
    @Ignore
    private final String formattedDistance;

    public EventInfo(Event event, AttractionFlag.Option option, String destinationName,
                     ArrayList<String> destinationCategories, Float distance, DestinationLocation location) {
        super(event, option);
        this.event = event;
        this.destinationName = destinationName;
        this.destinationCategories = destinationCategories;
        this.distance = distance;
        this.location = location;
        if (distance != null) {
            this.formattedDistance = numberFormatter.format(distance.floatValue()) + " mi";
        } else {
            this.formattedDistance = "";
        }
    }

    @Override
    public Event getAttraction() {
        return event;
    }

    @Override
    public Float getDistance() {
        return distance;
    }

    @Override
    public DestinationLocation getLocation() {
        return location;
    }

    @Override
    public String getFormattedDistance() {
        return formattedDistance;
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
