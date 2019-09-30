package org.gophillygo.app.data.models;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Holds the coordinates of a destination
 */

public class DestinationLocation {

    private final double x;
    private final double y;

    public DestinationLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @NonNull
    @Override
    public String toString() {
        return getY() + "," + getX();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DestinationLocation that = (DestinationLocation) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(x, y);
    }
}
