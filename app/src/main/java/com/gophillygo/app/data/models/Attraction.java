package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


@Entity
public class Attraction {

    @PrimaryKey
    private final int id;

    private final int placeID;
    private final String name;
    private final boolean accessible;
    private final String image;
    private final boolean cycling;
    private final String description;
    private final int priority;
    private final ArrayList<String> activities;

    @ColumnInfo(name = "website_url")
    @SerializedName("website_url")
    private final String websiteUrl;

    @ColumnInfo(name = "wide_image")
    @SerializedName("wide_image")
    private final String wideImage;

    @ColumnInfo(name = "is_event")
    @SerializedName("is_event")
    private final boolean isEvent;

    // timestamp is not final, as it is set on database save, and not by serializer
    private long timestamp;

    public Attraction(int id, int placeID, String name, boolean accessible, String image,
                      boolean cycling, String description, int priority, String websiteUrl,
                      String wideImage, boolean isEvent, ArrayList<String> activities) {
        this.id = id;
        this.placeID = placeID;
        this.name = name;
        this.accessible = accessible;
        this.image = image;
        this.cycling = cycling;
        this.description = description;
        this.priority = priority;
        this.websiteUrl = websiteUrl;
        this.wideImage = wideImage;
        this.isEvent = isEvent;

        this.activities = activities;
    }

    /**
     * Timestamp entries. Timestamp value does not come from query result; it should be set
     * on database save. Gson serializer will initialize value to zero.
     *
     * @param timestamp Time in milliseconds since Unix epoch
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getPlaceID() {
        return placeID;
    }

    public String getName() {
        return name;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public String getImage() {
        return image;
    }

    public boolean isCycling() {
        return cycling;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getWideImage() {
        return wideImage;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ArrayList<String> getActivities() {
        return activities;
    }
}
