package org.gophillygo.app.data.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import org.gophillygo.app.data.DestinationWebservice;

import java.util.ArrayList;
import java.util.Objects;


@Entity
public class Attraction {

    @Ignore
    private static final String LOG_LABEL = "Attraction";

    // Use _ID for the internal primary column name, to ease mapping to a Cursor.
    @PrimaryKey
    @ColumnInfo(index = true, name = "_id")
    @SerializedName("id")
    private final int _id;

    @ColumnInfo(index = true)
    private int placeID;

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

    @ColumnInfo(name = "extra_wide_images")
    @SerializedName("extra_wide_images")
    private final ArrayList<String> extraWideImages;

    // timestamp is not final, as it is set on database save, and not by serializer
    private long timestamp;

    public Attraction(int _id, int placeID, String name, boolean accessible, String image,
                      boolean cycling, String description, int priority, String websiteUrl,
                      String wideImage, boolean isEvent, ArrayList<String> activities,
                      ArrayList<String> extraWideImages) {
        this._id = _id;
        this.placeID = placeID;
        this.name = name;
        this.accessible = accessible;
        this.image = image;
        this.cycling = cycling;
        this.description = description;
        this.priority = priority;
        this.websiteUrl = websiteUrl != null ? websiteUrl : "";
        this.isEvent = isEvent;
        this.activities = activities;

        this.wideImage = wideImage;
        this.extraWideImages = extraWideImages;
    }


    /**
     * Helper to prepend host name to path; only needed in development with local server.
     *
     * @param path Image path, which may be relative
     * @return Full URL for image
     */
    private String addHostToPath(@NonNull String path) {
        if (!path.isEmpty() && !path.startsWith("http")) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return DestinationWebservice.WEBSERVICE_URL.concat(path);
        }
        return path;
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

    public boolean isHiking() {
        return activities.contains(CategoryAttraction.HIKING_API_NAME);
    }

    public boolean isWaterRecreation() {
        return activities.contains(CategoryAttraction.WATER_REC_API_NAME);
    }

    public int getId() {
        return _id;
    }

    public int getPlaceID() {
        return placeID;
    }

    public void setPlaceID(int placeID) {
        this.placeID = placeID;
    }

    public String getName() {
        return name;
    }

    public boolean isAccessible() {
        return accessible;
    }

    public String getImage() {
        return addHostToPath(image);
    }

    public boolean isCycling() {
        return cycling;
    }

    public String getDescription() {
        return description;
    }

    public Spanned getHtmlDescription() {
        return getHtmlFromString(description);
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
        return addHostToPath(wideImage);
    }

    public boolean isEvent() {
        return isEvent;
    }

    public ArrayList<String> getExtraWideImages() {
        ArrayList<String> imagesWithHost = new ArrayList<>(extraWideImages.size());
        for (String url : extraWideImages) {
            imagesWithHost.add(addHostToPath(url));
        }
        return imagesWithHost;
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

    /**
     * Helper to build an HTML span from a String, in a backwards-compatible way.
     *
     * @param source String representation of HTML
     * @return Parsed HTML in a span
     */
    @SuppressWarnings("deprecation")
    protected static Spanned getHtmlFromString(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    // Implement equals and hashcode for list adapter to diff.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attraction that = (Attraction) o;
        return _id == that._id &&
                placeID == that.placeID &&
                accessible == that.accessible &&
                cycling == that.cycling &&
                priority == that.priority &&
                isEvent == that.isEvent &&
                timestamp == that.timestamp &&
                Objects.equals(name, that.name) &&
                Objects.equals(image, that.image) &&
                Objects.equals(description, that.description) &&
                Objects.equals(activities, that.activities) &&
                Objects.equals(websiteUrl, that.websiteUrl) &&
                Objects.equals(wideImage, that.wideImage) &&
                Objects.equals(extraWideImages, that.extraWideImages);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_id, placeID, name, accessible, image, cycling, description, priority, activities, websiteUrl, wideImage, isEvent, timestamp);
    }
}
