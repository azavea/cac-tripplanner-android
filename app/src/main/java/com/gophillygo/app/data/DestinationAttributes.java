package com.gophillygo.app.data;

import android.arch.persistence.room.ColumnInfo;

/**
 * Extra destination attributes on a sub-property, including street address.
 */

public class DestinationAttributes {

    @ColumnInfo(name = "StAddr")
    private final String streetAddress;

    public DestinationAttributes(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStreetAddress() {
        return streetAddress;
    }
}
