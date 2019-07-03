package org.gophillygo.app.data.models;

import androidx.room.Embedded;

import java.util.Objects;

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

    @Override
    public DestinationLocation getLocation() {
        return destination != null ? destination.getLocation() : null;
    }

    @Override
    public Float getDistance() {
        return destination != null ? destination.getDistance() : null;
    }

    @Override
    public String getFormattedDistance() {
        return destination != null ? destination.getFormattedDistance() : null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DestinationInfo that = (DestinationInfo) o;
        return eventCount == that.eventCount &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), destination, eventCount);
    }
}
