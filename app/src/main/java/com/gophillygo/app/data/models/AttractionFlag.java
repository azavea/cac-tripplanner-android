package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;

@Entity
public class AttractionFlag {

    public enum Option {
        NotSelected (0),
        Liked (1),
        NotInterested (2),
        Been (3),
        WantToGo (4);

        private static final SparseArray<Option> map = new SparseArray<>();
        static {
            for (Option opt : Option.values()) {
                map.put(opt.code, opt);
            }
        }

        public final int code;

        Option(int code) {
            this.code = code;
        }

        public static Option valueOf(int code) {
            return map.get(code);
        }
    }

    @PrimaryKey
    private final int id;

    @ColumnInfo(index = true)
    private final int attractionID;

    @ColumnInfo(name = "is_event")
    @SerializedName("is_event")
    private final boolean isEvent;

    @TypeConverters(OptionConverter.class)
    private final Option option;

    public AttractionFlag(int id, int attractionID, boolean isEvent, Option option) {
        this.id = id;
        this.attractionID = attractionID;
        this.isEvent = isEvent;
        this.option = option;
    }

    public int getId() {
        return id;
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

    private static class OptionConverter {
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

