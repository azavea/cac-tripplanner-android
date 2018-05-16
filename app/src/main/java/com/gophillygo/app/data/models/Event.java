package com.gophillygo.app.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.arch.persistence.room.ForeignKey.SET_NULL;


@Entity(foreignKeys = @ForeignKey(entity = Destination.class,
        parentColumns = "id",
        childColumns = "destination",
        deferred = true,
        onDelete = SET_NULL),
        inheritSuperIndices = true)


public class Event extends Attraction {

    private static final String LOG_LABEL = "Event model";

    private static final DateFormat isoDateFormat;

    static {
        isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    }

    // using Integer instead of int so it may be nullable
    @ColumnInfo(index = true)
    private Integer destination;

    @ColumnInfo(name = "start_date", index = true)
    @SerializedName("start_date")
    private final String startDate;

    @ColumnInfo(name = "end_date", index = true)
    @SerializedName("end_date")
    private final String endDate;

    @Ignore
    private Date start, end;

    @Ignore
    private boolean isSingleDay;


    public Event(int id, int placeID, String name, boolean accessible, String image,
                 boolean cycling, String description, int priority, String websiteUrl,
                 String wideImage, boolean isEvent, ArrayList<String> activities,
                 Integer destination, String startDate, String endDate) {

        // initialize Attraction
        super(id, placeID, name, accessible, image, cycling, description, priority, websiteUrl,
              wideImage, isEvent, activities);

        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;

        try {
            this.start = isoDateFormat.parse(startDate);
        } catch (ParseException e) {
            this.start = null;
        }

        try {
            this.end = isoDateFormat.parse(endDate);
        } catch (ParseException e) {
            this.end = null;
        }

        this.isSingleDay = checkIfSingleDayEvent();

    }

    public Integer getDestination() {
        return destination;
    }

    public void setDestination(Integer id) {
        destination = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public boolean isSingleDayEvent() {
        return isSingleDay;
    }

    /**
     * Helper to set {@link Event#isSingleDay}.
     * Use {@link Event#isSingleDayEvent()} to access this attribute cached on the object.
     *
     * @return True if event starts and ends on the same day
     */
    private boolean checkIfSingleDayEvent() {

        if (this.start == null || this.end == null) {
            return true;
        }

        // check if event ends on same day as it starts
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(this.start);
        endCalendar.setTime(this.end);

        return startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) &&
                startCalendar.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR);
    }
}
