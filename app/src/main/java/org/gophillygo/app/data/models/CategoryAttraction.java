package org.gophillygo.app.data.models;

import android.support.annotation.IdRes;
import android.util.SparseArray;

import org.gophillygo.app.R;

import java.util.Objects;

/**
 * Model for the home screen categories, which display a random representative image from
 * places of a given category.
 */
public class CategoryAttraction {

    // category grid cards that are not in the destination category string: two user flags,
    // and events
    public static final String EVENTS_API_NAME = "events";
    public static final String NATURE_API_NAME = "Nature";
    public static final String EXERCISE_API_NAME = "Exercise";
    public static final String EDUCATIONAL_API_NAME = "Educational";
    public static final String CYCLING_API_NAME = "cycling";
    public static final String HIKING_API_NAME = "hiking";
    public static final String WATER_REC_API_NAME = "water recreation";
    public static final String WATERSHED_ALLIANCE = "watershed_alliance";

    public enum Activities {
        Cycling(CYCLING_API_NAME, R.string.cycling_activity_label),
        Hiking(HIKING_API_NAME, R.string.hiking_activity_label),
        WaterRecreation(WATER_REC_API_NAME, R.string.water_recreation_activity_label);

        private final @IdRes Integer displayName;
        private final String apiName;

        Activities(String apiName, Integer displayName) {
            this.apiName = apiName;
            this.displayName = displayName;
        }

        public Integer getDisplayName() {
            return displayName;
        }

        public String getApiName() {
            return apiName;
        }
    }

    /**
     * These are the categories that display on the home view
     */
    public enum PlaceCategories {
        Events(0, R.string.home_grid_events, EVENTS_API_NAME),
        WantToGo(1, R.string.place_want_to_go_option, AttractionFlag.Option.WantToGo.apiName),
        Liked(2, R.string.place_liked_option, AttractionFlag.Option.Liked.apiName),
        WatershedAlliance(3, R.string.watershed_alliance_label, WATERSHED_ALLIANCE),
        Nature(4, R.string.nature_category_label, NATURE_API_NAME),
        Exercise(5, R.string.exercise_category_label, EXERCISE_API_NAME),
        Educational(6, R.string.educational_category_label, EDUCATIONAL_API_NAME),
        Been(7, R.string.place_been_option, AttractionFlag.Option.Been.apiName);

        private static final SparseArray<PlaceCategories> map = new SparseArray<>();
        static {
            for (PlaceCategories category : PlaceCategories.values()) {
                map.put(category.code, category);
            }
        }

        public final int code;
        public final @IdRes Integer displayName;
        public final String dbName;

        PlaceCategories(int code, Integer displayName, String dbName) {
            this.code = code;
            this.displayName = displayName;
            this.dbName = dbName;
        }

        public static int size() {
            return map.size();
        }

        public static PlaceCategories valueOf(int code) {
            return map.get(code);
        }
    }

    private final PlaceCategories category;
    private final String image;

    public CategoryAttraction(int code, String image) {
        this.category = PlaceCategories.valueOf(code);
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public PlaceCategories getCategory() {
        return category;
    }

    public Integer getDisplayName() {
        return category.displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryAttraction)) return false;
        CategoryAttraction that = (CategoryAttraction) o;
        return category == that.category &&
                Objects.equals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, image);
    }
}
