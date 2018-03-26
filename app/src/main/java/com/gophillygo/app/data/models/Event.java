package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import static android.arch.persistence.room.ForeignKey.SET_NULL;


@Entity(foreignKeys = @ForeignKey(entity = Destination.class,
        parentColumns = "id",
        childColumns = "destination",
        deferred = true,
        onDelete = SET_NULL),
        inheritSuperIndices = true)


public class Event extends Attraction {

    // using Integer instead of int so it may be nullable
    @ColumnInfo(index = true)
    private final Integer destination;

    @ColumnInfo(name = "start_date", index = true)
    @SerializedName("start_date")
    private final String startDate;

    @ColumnInfo(name = "end_date", index = true)
    @SerializedName("end_date")
    private final String endDate;


    public Event(int id, int placeID, String name, boolean accessible, String image,
                 boolean cycling, String description, int priority, String websiteUrl,
                 String wideImage, boolean isEvent, ArrayList<String> activities,
                 Integer destination, String startDate, String endDate) {

        // initialize Attraction
        super(id, placeID, name, accessible, image, cycling, description, priority, websiteUrl,
                wideImage, isEvent, activities);

        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getDestination() {
        return destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}
