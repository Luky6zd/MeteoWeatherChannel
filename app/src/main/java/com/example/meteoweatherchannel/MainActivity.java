package com.example.meteoweatherchannel;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // initializing variables
    private RelativeLayout home;
    private ProgressBar loading;
    private TextView city, temperature, conditionView;
    private TextInputEditText cityEdit;
    private ImageView background, icon, searchIcon;
    private RecyclerView weather;
    private ArrayList<WeatherModel> weatherModelArrayList;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private final int PERMISSION_CODE = 1;
    private String citiesName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // app on full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        ViewPager2 viewPager2 = findViewById(R.id.viewPager);
        List<String> locations = Arrays.asList("New York", "Zagreb", "Paris");
        WeatherPagerAdapter adapter = new WeatherPagerAdapter(this, locations);
        viewPager2.setAdapter(adapter);

        // linking local variables with layout by its ID (defined in View)
        // findViewById returns a reference to the view
        home = findViewById(R.id.home);
        loading = findViewById(R.id.progressBarLoading);
        city = findViewById(R.id.cityName);
        temperature = findViewById(R.id.temperature);
        conditionView = findViewById(R.id.condition);
        cityEdit = findViewById(R.id.editCity);
        background = findViewById(R.id.background);
        icon = findViewById(R.id.icon);
        searchIcon = findViewById(R.id.searchIcon);
        weather = findViewById(R.id.weather);

        weatherModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        weather.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        weather.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            citiesName = getCityName(location.getLongitude(), location.getLatitude());
            getWeatherInfo(citiesName);
        } else {
            Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }


        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdit.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                } else {
                    MainActivity.this.city.setText(city);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Not found";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);

            assert addresses != null;
            for (Address address : addresses) {
                if (address != null) {
                    String city = address.getLocality();
                    if (city != null && !city.isEmpty()) {
                        cityName = city;
                    } else {
                        Log.d("TAG", "City not found!");
                        Toast.makeText(this, "City not found...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName) {
        String api_key = getString(R.string.weather_api_key);
        String api_key_url = "http://api.weatherapi.com/v1/forecast.json?key=" + api_key + "&q=" + cityName + "&days=1&aqi=no&alerts=no";
        city.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, api_key_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loading.setVisibility(View.GONE);
                home.setVisibility(View.VISIBLE);
                weatherModelArrayList.clear();

                try {
                    JSONObject current = response.getJSONObject("current");
                    String t = current.getString("temp_c");
                    temperature.setText(t + "°C");

                    int isDay = current.getInt("is_day");
                    String condition = current.getJSONObject("condition").getString("text");
                    String conditionIcon = current.getJSONObject("condition").getString("icon");

                    Picasso.get()
                            .load("http:" + conditionIcon)
                            .into(icon);

                    conditionView.setText(condition);

                    if (isDay == 1) {
                        // day
                        Picasso.get()
                                .load(R.drawable.weather_1)
                                .into(background);
                    } else {
                        // night
                        Picasso.get()
                                .load(R.drawable.weather_2)
                                .into(background);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastdayObj = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastdayObj.getJSONArray("hour");

                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        weatherModelArrayList.add(new WeatherModel(time, temp, img, wind));
                    }

                    weatherAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error processing the data...", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error fetching weather data. Try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}