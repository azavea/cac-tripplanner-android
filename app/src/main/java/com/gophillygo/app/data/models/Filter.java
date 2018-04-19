package com.gophillygo.app.data.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Filter implements Serializable {
    public static String NATURE_CATEGORY = "Nature";
    public static String EXERCISE_CATEGORY = "Exercise";
    public static String EDUCATIONAL_CATEGORY = "Educational";

    private boolean nature, exercise, educational, been, wantToGo, notInterested, liked, accessible;

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
        boolean categoryMatches = categories().isEmpty();
        if (destCategories == null) {
            return categoryMatches;
        }

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

    public boolean isNature() {
        return nature;
    }

    public void setNature(boolean nature) {
        this.nature = nature;
    }

    public boolean isExercise() {
        return exercise;
    }

    public void setExercise(boolean exercise) {
        this.exercise = exercise;
    }

    public boolean isEducational() {
        return educational;
    }

    public void setEducational(boolean educational) {
        this.educational = educational;
    }

    public boolean isBeen() {
        return been;
    }

    public void setBeen(boolean been) {
        this.been = been;
    }

    public boolean isWantToGo() {
        return wantToGo;
    }

    public void setWantToGo(boolean wantToGo) {
        this.wantToGo = wantToGo;
    }

    public boolean isNotInterested() {
        return notInterested;
    }

    public void setNotInterested(boolean notInterested) {
        this.notInterested = notInterested;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }
}
