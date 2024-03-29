package org.gophillygo.app.data.models;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.room.Ignore;
import androidx.room.TypeConverters;

import org.gophillygo.app.R;

import java.util.Objects;

public abstract class AttractionInfo<T extends Attraction> {
    @Ignore
    protected AttractionFlag flag;

    // This field is unused, but present to allow the constructor to receive a Option parameter,
    // which we use to initialize the AttractionFlag field
    @TypeConverters(AttractionFlag.OptionConverter.class)
    private AttractionFlag.Option option;

    AttractionInfo(T attraction, AttractionFlag.Option option) {
        if (option != null) {
            this.flag = new AttractionFlag(attraction.getId(), attraction.isEvent(), option);
        }
    }

    public abstract T getAttraction();
    public abstract DestinationLocation getLocation();
    public abstract Float getDistance();
    public abstract String getFormattedDistance();

    public AttractionFlag getFlag() {
        return flag;
    }

    public void setFlag(AttractionFlag flag) {
        this.flag = flag;
    }

    public @DrawableRes
    int getFlagImage() {
        return flag == null || flag.getOption() == null ? AttractionFlag.Option.NotSelected.drawable : flag.getOption().drawable;
    }

    public void updateAttractionFlag(@MenuRes int menuId) {
        AttractionFlag.Option option;
        if (menuId == R.id.place_option_not_interested) {
            option = AttractionFlag.Option.NotInterested;
        } else if (menuId == R.id.place_option_liked) {
            option = AttractionFlag.Option.Liked;
        } else if (menuId == R.id.place_option_been) {
            option = AttractionFlag.Option.Been;
        } else if (menuId == R.id.place_option_want_to_go) {
            option = AttractionFlag.Option.WantToGo;
        } else {
            option = AttractionFlag.Option.NotSelected;
        }
        // When selecting the option already selected, toggle it off
        if (flag != null && flag.getOption() == option) {
            option = AttractionFlag.Option.NotSelected;
        }
        this.flag = new AttractionFlag(getAttraction().getId(), getAttraction().isEvent(), option);
    }

    public AttractionFlag.Option getOption() {
        return option;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttractionInfo)) return false;
        AttractionInfo<?> that = (AttractionInfo<?>) o;
        return Objects.equals(flag, that.flag) &&
                option == that.option &&
                Objects.equals(getAttraction(), that.getAttraction());
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag, option);
    }
}
