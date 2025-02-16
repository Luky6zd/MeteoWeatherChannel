package com.example.meteoweatherchannel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// fragment displays weather details for single location
public class WeatherFragment extends Fragment {
    private static final String CITY = "city";
    private String city;
    private TextView temperatureTextView, locationTextView, weatherDescriptionTextView, timeTextView;
    private RecyclerView recyclerView;
    private WeatherFragmentAdapter adapter;
    private List<WeatherData> weatherList;
    private ImageView weatherIcon;

    // Constructor
    public static WeatherFragment newInstance(String city) {
        // Create new instance of fragment
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putString(CITY, city);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout for fragment
        View view = inflater.inflate(R.layout.weather_fragment, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize weather list and adapter
        weatherList = new ArrayList<>();
        adapter = new WeatherFragmentAdapter(weatherList);
        recyclerView.setAdapter(adapter);

        // Initialize Text Views elements
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        weatherDescriptionTextView = view.findViewById(R.id.weatherDescription);
        timeTextView = view.findViewById(R.id.timeTextView);
        ImageView weatherIcon = view.findViewById(R.id.weatherIcon);

        // Get city from arguments and fetch weather data
        assert getArguments() != null;
        city = getArguments().getString(CITY);
        ((MainActivity) requireActivity()).getWeatherInfo(city);

        //update weather data
        adapter.updateList(weatherList);

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assert getArguments() != null;
        String city = getArguments().getString("Solin");
        ((MainActivity) requireActivity()).getWeatherInfo(city);
    }

}
