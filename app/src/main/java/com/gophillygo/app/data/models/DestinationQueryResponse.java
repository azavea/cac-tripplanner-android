package com.gophillygo.app.data.models;

import java.util.List;

/**
 * Wrapper for the destinations query response, which contains destinations and events
 * as separate lists.
 */

public class DestinationQueryResponse {
    private final List<Destination> destinations;

    // TODO: create separate type for events
    private final List<Destination> events;

    public List<Destination> getDestinations() {
        return destinations;
    }

    @SuppressWarnings("unused")
    public DestinationQueryResponse(List<Destination> destinations, List<Destination> events) {
        this.destinations = destinations;
        this.events = events;
    }
}
