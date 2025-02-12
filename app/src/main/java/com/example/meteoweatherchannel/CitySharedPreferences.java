package com.example.meteoweatherchannel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CitySharedPreferences {
    private static final String PREFS_NAME = "CityPrefs";
    private static final String KEY_CITIES = "cities";

    // Spremi listu gradova
    public static void saveCities(Context context, List<String> cities) {
        // SharedPreferences za spremanje
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Editor za spremanje
        SharedPreferences.Editor editor = prefs.edit();

        // Pretvori listu u Set za pohranu
        Set<String> set = new HashSet<>(cities);
        editor.putStringSet(KEY_CITIES, set);

        // Asinkrono spremanje
        editor.apply();
    }

    // Metoda za dobivanje spremljenih gradova
    public static List<String> getCities(Context context) {
        // SharedPreferences za getanje
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Dohvati set i odmah ga pretvori u novi HashSet da bude "mutable"
        Set<String> set = prefs.getStringSet(KEY_CITIES, new HashSet<>());

        // Vrati listu gradova
        return new ArrayList<>(set);
    }
}
