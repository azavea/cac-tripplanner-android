package org.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;
import android.util.Log;
import android.content.Context;

import org.gophillygo.app.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventInfo extends AttractionInfo<Event> {

    private static final String LOG_LABEL = "EventInfo";

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

    @ColumnInfo(index = true, name = "watershed_alliance")
    private final boolean watershedAlliance;

    public EventInfo(Event event, String destinationName, ArrayList<String> destinationCategories,
                     AttractionFlag.Option option, Float distance, DestinationLocation location,
                     boolean watershedAlliance) {
        super(event, option);
        this.event = event;
        this.destinationName = destinationName;
        this.destinationCategories = destinationCategories;
        this.distance = distance;
        this.location = location;
        this.watershedAlliance = watershedAlliance;

        if (destinationCategories != null && !destinationCategories.isEmpty()) {
            this.categories = new DestinationCategories(destinationCategories.contains(CategoryAttraction.PlaceCategories.Nature.dbName),
                    destinationCategories.contains(CategoryAttraction.PlaceCategories.Exercise.dbName),
                    destinationCategories.contains(CategoryAttraction.PlaceCategories.Educational.dbName));
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
    public String getFormattedDistance(Context context) {
        if (distance == null) {
            return "";
        }
        return numberFormatter.format(distance.floatValue());
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

    public boolean isWatershedAlliance() {
        return watershedAlliance;
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
                Objects.equals(distance, eventInfo.distance) &&
                Objects.equals(watershedAlliance, eventInfo.watershedAlliance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), event, destinationName, destinationCategories, distance, watershedAlliance);
    }
}
