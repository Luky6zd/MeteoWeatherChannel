package com.example.meteoweatherchannel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// fragment that display weather details for a single location
public class WeatherFragment extends Fragment {
    private static final String ARG_LOCATION = "location";

    public static WeatherFragment newWeatherFragment(String location) {
        WeatherFragment fragment = new WeatherFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, location);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.weather_fragment, container, false);

        String location = getArguments().getString(ARG_LOCATION);
        TextView locationTextView = view.findViewById(R.id.locationTextView);
        locationTextView.setText(location);

        // Fetch weather data for the location and update UI...

        return view;
    }
}
