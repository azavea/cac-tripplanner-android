package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
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
    @Ignore
    private final DestinationCategories categories;

    @Embedded
    private final DestinationLocation location;

    @ColumnInfo(index = true)
    private final Float distance;
    @Ignore
    private final String formattedDistance;

    public EventInfo(Event event, String destinationName, ArrayList<String> destinationCategories,
                     AttractionFlag.Option option, Float distance, DestinationLocation location) {
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

        if (destinationCategories != null && !destinationCategories.isEmpty()) {
            this.categories = new DestinationCategories(destinationCategories.contains(Filter.NATURE_CATEGORY),
                    destinationCategories.contains(Filter.EXERCISE_CATEGORY),
                    destinationCategories.contains(Filter.EDUCATIONAL_CATEGORY));
        } else {
            this.categories = new DestinationCategories(false, false, false);
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

    public DestinationCategories getCategories() {
        return categories;
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
