package com.example.meteoweatherchannel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CitySharedPreferences {
    // name of SharedPreferences
    private static final String PREFS_NAME = "CityPrefs";
    // key for saving list of cities
    private static final String KEY_CITIES = "cities";

    // save list of cities in SharedPreferences
    public static void saveCities(Context context, List<String> cities) {
        // saving in SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // editor
        SharedPreferences.Editor editor = prefs.edit();

        // convert list of cities to set
        Set<String> set = new HashSet<>(cities);
        editor.putStringSet(KEY_CITIES, set);

        // async apply changes
        editor.apply();
    }

    // get list of cities from SharedPreferences
    public static List<String> getCities(Context context) {
        // read list of cities from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(KEY_CITIES, new HashSet<>());

        // return list of cities
        return new ArrayList<>(set);
    }
}
