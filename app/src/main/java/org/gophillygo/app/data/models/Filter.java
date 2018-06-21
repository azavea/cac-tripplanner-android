package org.gophillygo.app.data.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.util.Log;

import org.gophillygo.app.BR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Filter extends BaseObservable implements Serializable {

    private static final String LOG_LABEL = "Filter";

    public static final String NATURE_CATEGORY = "Nature";
    public static final String EXERCISE_CATEGORY = "Exercise";
    public static final String EDUCATIONAL_CATEGORY = "Educational";

    @Bindable
    private boolean nature;
    @Bindable
    private boolean exercise;
    @Bindable
    private boolean educational;
    @Bindable
    private boolean been;
    @Bindable
    private boolean wantToGo;
    @Bindable
    private boolean notInterested;
    @Bindable
    private boolean liked;
    @Bindable
    private boolean accessible;

    public Filter() {
        this(false, false, false, false, false, false, false, false);
    }

    public Filter(Filter other) {
        this(other.nature, other.exercise, other.educational, other.been, other.wantToGo, other.notInterested, other.liked, other.accessible);
    }

    public Filter(boolean nature, boolean exercise, boolean educational, boolean been,
                  boolean wantToGo, boolean notInterested, boolean liked, boolean accessible) {
        this.nature = nature;
        this.exercise = exercise;
        this.educational = educational;
        this.been = been;
        this.wantToGo = wantToGo;
        this.notInterested = notInterested;
        this.liked = liked;
        this.accessible = accessible;
    }

    public int count() {
        boolean[] fields = {nature, exercise, educational, been, wantToGo, notInterested, liked, accessible};
        int selectedCount = 0;
        for (boolean field : fields) {
            if (field) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    public void reset() {
        this.nature = false;
        this.exercise = false;
        this.educational = false;
        this.been = false;
        this.wantToGo = false;
        this.notInterested = false;
        this.liked = false;
        this.accessible = false;
        notifyChange();
    }

    public boolean matches(DestinationInfo info) {
        DestinationCategories flags = info.getDestination().getCategoryFlags();
        if (flags == null) {
            Log.e(LOG_LABEL, "Category flags are missing for destination " + info.getDestination().getName());
            return false;
        }
        boolean categoryMatches = categoryMatches(flags);
        boolean flagMatches = flagMatches(info.getFlag());
        boolean accessibleMatches = accessibleMatches(info.getDestination().isAccessible());

        return categoryMatches && flagMatches && accessibleMatches;
    }

    public boolean matches(EventInfo info) {
        boolean categoryMatches = categoryMatches(info.getCategories());
        boolean flagMatches = flagMatches(info.getFlag());
        boolean accessibleMatches = accessibleMatches(info.getEvent().isAccessible());

        return categoryMatches && flagMatches && accessibleMatches;
    }

    private boolean categoryMatches(DestinationCategories categories) {
        // match all if not filtering by category
        if (!nature && !exercise && !educational) {
            return true;
        }
        // match on any filter category
        return ((nature && categories.isNature()) || (educational && categories.isEducational()) ||
                (exercise && categories.isExercise()));
    }

    private boolean flagMatches(AttractionFlag flag) {
        boolean flagMatches = flags().isEmpty();
        for (AttractionFlag.Option option : flags()) {
            if (flag.getOption() == option) {
                flagMatches = true;
            }
        }
        return flagMatches;
    }

    private boolean accessibleMatches(boolean isAccessible) {
        boolean accessibleMatches = true;
        if (accessible && !isAccessible) {
            accessibleMatches = false;
        }
        return accessibleMatches;
    }

    private List<AttractionFlag.Option> flags() {
        List<AttractionFlag.Option> flags = new ArrayList<>();
        if (been) {
            flags.add(AttractionFlag.Option.Been);
        }
        if (liked) {
            flags.add(AttractionFlag.Option.Liked);
        }
        if (notInterested) {
            flags.add(AttractionFlag.Option.NotInterested);
        }
        if (wantToGo) {
            flags.add(AttractionFlag.Option.WantToGo);
        }
        return flags;
    }

    public boolean isNature() {
        return nature;
    }

    public void setNature(boolean nature) {
        this.nature = nature;
        notifyPropertyChanged(BR.nature);
    }

    public boolean isExercise() {
        return exercise;
    }

    public void setExercise(boolean exercise) {
        this.exercise = exercise;
        notifyPropertyChanged(BR.exercise);
    }

    public boolean isEducational() {
        return educational;
    }

    public void setEducational(boolean educational) {
        this.educational = educational;
        notifyPropertyChanged(BR.educational);
    }

    public boolean isBeen() {
        return been;
    }

    public void setBeen(boolean been) {
        this.been = been;
        notifyPropertyChanged(BR.been);
    }

    public boolean isWantToGo() {
        return wantToGo;
    }

    public void setWantToGo(boolean wantToGo) {
        this.wantToGo = wantToGo;
        notifyPropertyChanged(BR.wantToGo);
    }

    public boolean isNotInterested() {
        return notInterested;
    }

    public void setNotInterested(boolean notInterested) {
        this.notInterested = notInterested;
        notifyPropertyChanged(BR.notInterested);
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
        notifyPropertyChanged(BR.liked);
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
        notifyPropertyChanged(BR.accessible);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filter filter = (Filter) o;
        return nature == filter.nature &&
                exercise == filter.exercise &&
                educational == filter.educational &&
                been == filter.been &&
                wantToGo == filter.wantToGo &&
                notInterested == filter.notInterested &&
                liked == filter.liked &&
                accessible == filter.accessible;
    }

    @Override
    public int hashCode() {

        return Objects.hash(nature, exercise, educational, been, wantToGo, notInterested, liked, accessible);
    }
}
