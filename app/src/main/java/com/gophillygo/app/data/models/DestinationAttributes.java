package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

/**
 * Extra destination attributes on a sub-property, including street address.
 */

public class DestinationAttributes {

    @ColumnInfo(name = "street_address")
    @SerializedName("StAddr")
    private final String streetAddress;

    public DestinationAttributes(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStreetAddress() {
        return streetAddress;
    }
}
