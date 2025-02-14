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

// fragment display weather details for single location
public class WeatherFragment extends Fragment {
    private static final String CITY = "city";
    private String city;
    private TextView tvTime, tvTemperature, tvWindSpeed;
    private RecyclerView recyclerView;
    private WeatherFragmentAdapter adapter;
    private List<WeatherData> weatherList;
    private ImageView searchIcon;

    public static WeatherFragment newInstance(String city) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putString(CITY, city);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.weather_fragment, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create dummy data
        weatherList = new ArrayList<>();

        // Set up the adapter
        adapter = new WeatherFragmentAdapter(weatherList);
        recyclerView.setAdapter(adapter);

        // Inicijalizacija TextView elemenata
        tvTime = view.findViewById(R.id.tvTime);
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed);


        city = getArguments().getString(CITY);
        ((MainActivity) requireActivity()).getWeatherInfo(city);

        //fetchWeather(city, view); // dohvat vremenskih podataka

        return view;
    }

    public String getCity() {
        return city;
    }

    private void fetchWeather(String city, View view) {
        String api_key = getString(R.string.weather_api_key);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=" + api_key + "&q=" + city + "&days=1";

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Parsiranje odgovora
                    Gson gson = new Gson();
                    try {
                        String time = response.getJSONObject("location").getString("localtime");
                        String temperature = response.getJSONObject("current").getString("temp_c");
                        String windSpeed = response.getJSONObject("current").getString("wind_kph");
                        String icon = response.getJSONObject("current").getJSONObject("condition").getString("icon");

                        WeatherData data = new WeatherData(time, temperature, icon, windSpeed);
                        updateUI(data); // Ažuriranje UI s podacima
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    }

                    WeatherData data = gson.fromJson(response.toString(), WeatherData.class);

                    TextView tvCity = view.findViewById(R.id.editTextCity);
                    TextView tvTemp = view.findViewById(R.id.tvTemperature);
                    TextView tvWind = view.findViewById(R.id.tvWindSpeed);
                    TextView tvIcon = view.findViewById(R.id.searchIcon);

                    // Postavljanje podataka u UI
                    tvCity.setText(data.getLocation().getLocationName());
                    tvTemp.setText(data.getTemperature() + "°C");
                    tvWind.setText(data.getWindSpeed() + " km/h");
                    tvIcon.setText(data.getIcon());
                },
                error -> Toast.makeText(getContext(), "Error getting weather data" + error.getMessage(), Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }


    private void updateUI(WeatherData data) {
        tvTime.setText("Vrijeme: " + data.getTime());
        tvTemperature.setText(data.getTemperature() + "°C");
        tvWindSpeed.setText("Vjetar: " + data.getWindSpeed() + " km/h");
    }
}
