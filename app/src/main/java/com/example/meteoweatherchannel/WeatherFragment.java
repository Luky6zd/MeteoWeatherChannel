package com.example.meteoweatherchannel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

// fragment displays weather details for single location
public class WeatherFragment extends Fragment {
    private static final String CITY = "city";
    private String city;
    private TextView tvTime, tvTemperature, tvWindSpeed;
    private RecyclerView recyclerView;
    private WeatherFragmentAdapter adapter;
    private List<WeatherData> weatherList;
    private ImageView searchIcon;

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
        tvTime = view.findViewById(R.id.tvTime);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed);
        ImageView searchIcon = view.findViewById(R.id.weatherIcon);

        // Get city from arguments and fetch weather data
        assert getArguments() != null;
        city = getArguments().getString(CITY);
        ((MainActivity) requireActivity()).getWeatherInfo(city);

        //update weather data
        adapter.updateList(weatherList);

        return view;
    }
}
