package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;

import com.google.gson.annotations.SerializedName;

import java.text.NumberFormat;
import java.util.ArrayList;


@Entity(inheritSuperIndices = true)
public class Destination extends Attraction {

    private static final NumberFormat numberFormatter = NumberFormat.getNumberInstance();
    static {
        numberFormatter.setMinimumFractionDigits(0);
        numberFormatter.setMaximumFractionDigits(2);
    }

    private final String city;
    private final String state;
    private final String address;

    private final ArrayList<String> categories;

    @Embedded
    private final DestinationLocation location;

    @ColumnInfo(name = "watershed_alliance")
    @SerializedName("watershed_alliance")
    private final boolean watershedAlliance;

    @Embedded
    private final DestinationAttributes attributes;

    @ColumnInfo(name = "zipcode")
    @SerializedName("zipcode")
    private final String zipCode;

    // convenience property to track distance to each destination
    private float distance;
    private String formattedDistance;

    public Destination(int id, int placeID, String name, boolean accessible, String image,
                       String city, boolean cycling, String zipCode, String description,
                       int priority, String state, String address, DestinationLocation location,
                       DestinationAttributes attributes, boolean watershedAlliance, String websiteUrl,
                       String wideImage, boolean isEvent, ArrayList<String> activities,
                       ArrayList<String> categories) {

        // initialize Attraction
        super(id, placeID, name, accessible, image, cycling, description, priority, websiteUrl,
                wideImage, isEvent, activities);

        this.city = city;
        this.zipCode = zipCode;
        this.state = state;
        this.address = address;
        this.watershedAlliance = watershedAlliance;

        this.location = location;
        this.attributes = attributes;
        this.categories = categories;
    }

    public void setDistance(float distance) {
        this.distance = distance;
        this.formattedDistance = numberFormatter.format(distance) + " mi";
    }

    // setFormattedDistance is here to please compiler; it is better to set via setDistance
    public void setFormattedDistance(String formattedDistance) {
        this.formattedDistance = formattedDistance;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getAddress() {
        return address;
    }

    public DestinationLocation getLocation() {
        return location;
    }

    public DestinationAttributes getAttributes() {
        return attributes;
    }

    public String getZipCode() {
        return zipCode;
    }

    public float getDistance() {
        return distance;
    }

    public String getFormattedDistance() {
        return formattedDistance;
    }

    public boolean isWatershedAlliance() {
        return watershedAlliance;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }
}
