package org.gophillygo.app.data.models;

import java.util.Objects;

public class DestinationCategories {
    private boolean nature;
    private boolean exercise;
    private boolean educational;

    public DestinationCategories(boolean nature, boolean exercise, boolean educational) {
        this.nature = nature;
        this.exercise = exercise;
        this.educational = educational;
    }

    public boolean isNature() {
        return nature;
    }

    public boolean isExercise() {
        return exercise;
    }

    public boolean isEducational() {
        return educational;
    }

    public void setNature(boolean nature) {
        this.nature = nature;
    }

    public void setExercise(boolean exercise) {
        this.exercise = exercise;
    }

    public void setEducational(boolean educational) {
        this.educational = educational;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DestinationCategories)) return false;
        DestinationCategories that = (DestinationCategories) o;
        return nature == that.nature &&
                exercise == that.exercise &&
                educational == that.educational;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nature, exercise, educational);
    }
}
