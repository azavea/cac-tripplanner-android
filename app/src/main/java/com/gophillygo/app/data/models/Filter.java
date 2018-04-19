package com.gophillygo.app.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Filter implements Parcelable {
    public static String NATURE_CATEGORY = "Nature";
    public static String EXERCISE_CATEGORY = "Exercise";
    public static String EDUCATIONAL_CATEGORY = "Educational";

    public boolean nature, exercise, educational, been, wantToGo, notInterested, liked, accessible;

    public Filter() {
        this(false, false, false, false, false, false, false, false);
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

    private Filter(Parcel in) {
        boolean[] fields = in.createBooleanArray();
        this.nature = fields[0];
        this.exercise = fields[1];
        this.educational = fields[2];
        this.been = fields[3];
        this.wantToGo = fields[4];
        this.notInterested = fields[5];
        this.liked = fields[6];
        this.accessible = fields[7];
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

    public boolean matches(DestinationInfo info) {
        boolean categoryMatches = categoryMatches(info.getDestination().getCategories());
        boolean flagMatches = flagMatches(info.getFlag());
        boolean accessibleMatches = accessibleMatches(info.getDestination().isAccessible());

        return categoryMatches && flagMatches && accessibleMatches;
    }

    public boolean matches(EventInfo info) {
        boolean categoryMatches = categoryMatches(info.getDestinationCategories());
        boolean flagMatches = flagMatches(info.getFlag());
        boolean accessibleMatches = accessibleMatches(info.getEvent().isAccessible());

        return categoryMatches && flagMatches && accessibleMatches;
    }

    private boolean categoryMatches(List<String> destCategories) {
        if (destCategories == null) return false;

        boolean categoryMatches = categories().isEmpty();
        for (String category : categories()) {
            if (destCategories.contains(category)) {
                categoryMatches = true;
            }
        }
        return categoryMatches;
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

    private List<String> categories() {
        List<String> categories = new ArrayList<>();
        if (nature) {
            categories.add(NATURE_CATEGORY);
        }
        if (exercise) {
            categories.add(EXERCISE_CATEGORY);
        }
        if (educational) {
            categories.add(EDUCATIONAL_CATEGORY);
        }
        return categories;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBooleanArray(new boolean[]{nature, exercise, educational, been, wantToGo,
                                               notInterested, liked, accessible});
    }

    public static final Parcelable.Creator<Filter> CREATOR = new Parcelable.Creator<Filter>() {
        public Filter createFromParcel(Parcel in) {
            return new Filter(in);
        }

        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };
}
