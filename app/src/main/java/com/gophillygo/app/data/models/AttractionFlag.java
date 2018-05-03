package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;
import com.gophillygo.app.R;

@Entity(primaryKeys = {"attractionID", "is_event"})
public class AttractionFlag {

    public enum Option {
        NotSelected (0, R.drawable.ic_add_black_24dp, null),
        Liked (1, R.drawable.ic_thumb_up_black_24dp, R.id.place_option_liked),
        NotInterested (2, R.drawable.ic_not_interested_black_24dp, R.id.place_option_not_interested),
        Been (3, R.drawable.ic_beenhere_black_24dp, R.id.place_option_been),
        WantToGo (4, R.drawable.ic_flag_black_24dp, R.id.place_option_want_to_go);

        private static final SparseArray<Option> map = new SparseArray<>();
        static {
            for (Option opt : Option.values()) {
                map.put(opt.code, opt);
            }
        }

        public final int code;
        public final @DrawableRes int drawable;
        public final @IdRes Integer id;

        Option(int code, @DrawableRes int drawable, @IdRes Integer id) {
            this.code = code;
            this.drawable = drawable;
            this.id = id;
        }

        public static Option valueOf(int code) {
            return map.get(code);
        }
    }

    @ColumnInfo(index = true)
    private final int attractionID;

    @ColumnInfo(name = "is_event", index = true)
    @SerializedName("is_event")
    private final boolean isEvent;

    @TypeConverters(OptionConverter.class)
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
}

