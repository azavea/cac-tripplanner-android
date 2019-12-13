package org.gophillygo.app.data;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.gophillygo.app.data.models.Destination;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class RoomConverters {

    @TypeConverter
    public static ArrayList<String> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String arraylistToString(ArrayList<String> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public static ArrayList<Destination> fromDestination(String value) {
        Type listType = new TypeToken<ArrayList<Destination>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String arraylistToDestination(ArrayList<Destination> list) {
        return new Gson().toJson(list);
    }
}