package com.gophillygo.app.data.models;

import java.util.List;
import java.util.Objects;

/**
 * Wrapper for the destinations query response, which contains destinations and events
 * as separate lists.
 */

public class DestinationQueryResponse {
    private final List<Destination> destinations;

    private final List<Event> events;

    public List<Destination> getDestinations() {
        return destinations;
    }

    public List<Event> getEvents() { return events; }

    @SuppressWarnings("unused")
    public DestinationQueryResponse(List<Destination> destinations, List<Event> events) {
        this.destinations = destinations;
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DestinationQueryResponse that = (DestinationQueryResponse) o;
        return Objects.equals(destinations, that.destinations) &&
                Objects.equals(events, that.events);
    }

    @Override
    public int hashCode() {

        return Objects.hash(destinations, events);
    }
}
