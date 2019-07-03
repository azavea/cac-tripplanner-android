package org.gophillygo.app.data.models;

import android.util.SparseArray;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import org.gophillygo.app.R;

import java.util.Objects;

@Entity(primaryKeys = {"attraction_id", "is_event"},
        indices = {@Index(value = {"is_event", "option"})})
public class AttractionFlag {

    public enum Option {
        NotSelected (0, R.drawable.ic_add_black_24dp, null, "",
                R.string.place_detail_unset, R.string.event_detail_unset),
        Liked (1, R.drawable.ic_thumb_up_blue_24dp, R.id.place_option_liked, "liked",
                R.string.place_detail_liked, R.string.event_detail_liked),
        NotInterested (2, R.drawable.ic_not_interested_blue_24dp, R.id.place_option_not_interested, "not_interested",
                R.string.place_detail_not_interested, R.string.event_detail_not_interested),
        Been (3, R.drawable.ic_beenhere_blue_24dp, R.id.place_option_been, "been",
                R.string.place_detail_been, R.string.event_detail_been),
        WantToGo (4, R.drawable.ic_flag_blue_24dp, R.id.place_option_want_to_go, "want_to_go",
                R.string.place_detail_want_to_go, R.string.event_detail_want_to_go);

        private static final SparseArray<Option> map = new SparseArray<>();
        static {
            for (Option opt : Option.values()) {
                map.put(opt.code, opt);
            }
        }

        public final int code;
        public final @DrawableRes
        int drawable;
        public final @IdRes
        Integer id;
        public final String apiName;
        public final @StringRes
        Integer placeLabel;
        public final @StringRes Integer eventLabel;

        Option(int code, @DrawableRes int drawable, @IdRes Integer id, String apiName,
               @StringRes Integer placeLabel, @StringRes Integer eventLabel) {
            this.code = code;
            this.drawable = drawable;
            this.id = id;
            this.apiName = apiName;
            this.placeLabel = placeLabel;
            this.eventLabel = eventLabel;
        }

        public static Option valueOf(int code) {
            return map.get(code);
        }
    }

    @ColumnInfo(name = "attraction_id", index = true)
    private final int attractionID;

    @ColumnInfo(name = "is_event", index = true)
    @SerializedName("is_event")
    private final boolean isEvent;

    @TypeConverters(OptionConverter.class)
    @ColumnInfo(index = true)
    private final Option option;

    public AttractionFlag(int attractionID, boolean isEvent, Option option) {
        this.attractionID = attractionID;
        this.isEvent = isEvent;
        this.option = option;
    }

    public int getAttractionID() {
        return attractionID;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public Option getOption() {
        return option;
    }

    public static class OptionConverter {
        @TypeConverter
        public static Option toOption(int code) {
            return Option.valueOf(code);
        }

        @TypeConverter
        public static int toInt(Option option) {
            return option.code;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttractionFlag that = (AttractionFlag) o;
        return attractionID == that.attractionID &&
                isEvent == that.isEvent &&
                option == that.option;
    }

    @Override
    public int hashCode() {
        return Objects.hash(attractionID, isEvent, option);
    }
}

