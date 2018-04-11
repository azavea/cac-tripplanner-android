package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.text.Html;
import android.text.Spanned;

import com.google.gson.annotations.SerializedName;
import com.gophillygo.app.R;

import java.util.ArrayList;


@Entity
public class Attraction {

    @PrimaryKey
    private final int id;

    @ColumnInfo(index = true)
    private final int placeID;

    @ColumnInfo(index = true)
    private final String name;

    @ColumnInfo(index = true)
    private final boolean accessible;
    private final String image;

    @ColumnInfo(index = true)
    private final boolean cycling;
    private final String description;
    private final int priority;

    @ColumnInfo(index = true)
    private final ArrayList<String> activities;

    @ColumnInfo(name = "website_url")
    @SerializedName("website_url")
    private final String websiteUrl;

    @ColumnInfo(name = "wide_image")
    @SerializedName("wide_image")
    private final String wideImage;

    @ColumnInfo(name = "is_event")
    @SerializedName("is_event")
    private final boolean isEvent;

    // timestamp is not final, as it is set on database save, and not by serializer
    private long timestamp;

    // Stored separately from Attraction models and set afterwards
    @Ignore
    private AttractionFlag flag;

    public Attraction(int id, int placeID, String name, boolean accessible, String image,
                      boolean cycling, String description, int priority, String websiteUrl,
                      String wideImage, boolean isEvent, ArrayList<String> activities) {
        this.id = id;
        this.placeID = placeID;
        this.name = name;
        this.accessible = accessible;
        this.image = image;
        this.cycling = cycling;
        this.description = description;
        this.priority = priority;
        this.websiteUrl = websiteUrl;
        this.wideImage = wideImage;
        this.isEvent = isEvent;

        this.activities = activities;
    }

    /**
     * User options for attraction flags. Does not come from query results and is stored in a
     * separate table.
     *
     * @param flag user flag
     */
    public void setFlag(AttractionFlag flag) {
        this.flag = flag;
    }

    /**
     * Timestamp entries. Timestamp value does not come from query result; it should be set
     * on database save. Gson serializer will initialize value to zero.
     *
     * @param timestamp Time in milliseconds since Unix epoch
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getPlaceID() {
        return placeID;
    }

    public String getName() {
        return name;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public String getImage() {
        return image;
    }

    public boolean isCycling() {
        return cycling;
    }

    public String getDescription() {
        return description;
    }

    public Spanned getHtmlDescription() {
        return Html.fromHtml(description);
    }

    public int getPriority() {
        return priority;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public boolean hasWebsite() {
        return !websiteUrl.isEmpty();
    }

    public String getWideImage() {
        return wideImage;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public AttractionFlag getFlag() {
        return flag;
    }

    public @DrawableRes int getFlagImage() {
        return flag == null || flag.getOption() == null ? AttractionFlag.Option.NotSelected.drawable : flag.getOption().drawable;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ArrayList<String> getActivities() {
        return activities;
    }

    public boolean hasActivities() {
        return activities.size() > 0;
    }

    // get a dot-separated string listing all the activities available here
    public String getActivitiesString() {
        StringBuilder stringBuilder = new StringBuilder("");
        // separate activities with dots
        String dot = Html.fromHtml("&nbsp;&#8226;&nbsp;").toString();
        for (String activity: this.getActivities()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(dot);
            }
            stringBuilder.append(activity);
        }
        return stringBuilder.toString();
    }

    public AttractionFlag createAttractionFlag(@MenuRes int menuId) {
        AttractionFlag.Option option;
        switch (menuId) {
            case R.id.place_option_not_interested:
                option = AttractionFlag.Option.NotInterested;
                break;
            case R.id.place_option_liked:
                option = AttractionFlag.Option.Liked;
                break;
            case R.id.place_option_been:
                option = AttractionFlag.Option.Been;
                break;
            case R.id.place_option_want_to_go:
                option = AttractionFlag.Option.WantToGo;
                break;
            default:
                option = AttractionFlag.Option.NotSelected;
        }
        if (flag != null) {
            // When selecting the option already selected, toggle it off
            if (flag.getOption() == option) {
                option = AttractionFlag.Option.NotSelected;
            }
            return new AttractionFlag(flag.getId(), getId(), isEvent(), option);
        }
        return new AttractionFlag(null, getId(), isEvent(), option);
    }
}
