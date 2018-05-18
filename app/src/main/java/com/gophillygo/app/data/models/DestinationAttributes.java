package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DestinationAttributes that = (DestinationAttributes) o;
        return Objects.equals(streetAddress, that.streetAddress);
    }

    @Override
    public int hashCode() {

        return Objects.hash(streetAddress);
    }
}
