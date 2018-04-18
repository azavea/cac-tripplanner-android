package com.gophillygo.app.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Filter implements Parcelable {
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

    public List<String> categories() {
        List<String> categories = new ArrayList<>();
        if (nature) {
            categories.add("Nature");
        }
        if (exercise) {
            categories.add("Exercise");
        }
        if (educational) {
            categories.add("Educational");
        }
        return categories;

    }

    public List<Integer> flags() {
        List<Integer> flags = new ArrayList<>();
        if (been) {
            flags.add(AttractionFlag.Option.Been.code);
        }
        if (liked) {
            flags.add(AttractionFlag.Option.Liked.code);
        }
        if (notInterested) {
            flags.add(AttractionFlag.Option.NotInterested.code);
        }
        if (wantToGo) {
            flags.add(AttractionFlag.Option.WantToGo.code);
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
