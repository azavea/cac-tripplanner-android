package org.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

/**
 * Minimal model for representative images chosen from a filter category.
 */
public class CategoryImage {

    private final int attractionID;
    @ColumnInfo(name = "is_event")
    private final boolean isEvent;
    private final String image;

    public CategoryImage(int attractionID, boolean isEvent, String image) {

        this.attractionID = attractionID;
        this.isEvent = isEvent;
        this.image = image;
    }

    public int getAttractionID() {
        return attractionID;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public String getImage() {
        return image;
    }
}
