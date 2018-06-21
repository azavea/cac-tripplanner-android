package org.gophillygo.app.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Model representation of the expected format for the user flag post endpoint.
 */
public class UserFlagPost {

    // The attraction ID. Note: this is `id` and not `placeID`
    private final int attraction;

    // The flag key name (not the user-presentable name)
    private final String flag;

    @SerializedName("is_event")
    private final boolean isEvent;

    @SerializedName("user_uuid")
    private final String userUuid;

    @SerializedName("api_key")
    private final String apiKey;

    public UserFlagPost(int attraction, String flag, boolean isEvent, String userUuid, String apiKey) {
        this.attraction = attraction;
        this.flag = flag;
        this.isEvent = isEvent;
        this.userUuid = userUuid;
        this.apiKey = apiKey;
    }

    public int getAttraction() {
        return attraction;
    }

    public String getFlag() {
        return flag;
    }

    public boolean isEvent() {
        return isEvent;
    }

    public String getUserUuid() {
        return userUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserFlagPost)) return false;
        UserFlagPost that = (UserFlagPost) o;
        return attraction == that.attraction &&
                isEvent == that.isEvent &&
                Objects.equals(flag, that.flag) &&
                Objects.equals(userUuid, that.userUuid) &&
                Objects.equals(apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {

        return Objects.hash(attraction, flag, isEvent, userUuid, apiKey);
    }
}
