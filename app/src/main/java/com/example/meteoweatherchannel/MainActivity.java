package com.example.meteoweatherchannel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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
    private WeatherPagerAdapter weatherPagerAdapter;
    private ViewPager2 viewPager2;
    private SharedPreferences prefs;
    private WeatherFragmentAdapter weatherFragmentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // app on full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        // Initialisation of SharedPreferences
        prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);
        // Initialisation of ViewPager2
        viewPager2 = findViewById(R.id.viewPager);
        // Initialisation of Weather Pager Adapter
        List<String> cities = new ArrayList<>(Arrays.asList("Solin", "Split", "Zagreb", "Osijek", "Rijeka", "Karlovac", "Vukovar", "Zadar", "Pula", "Dubrovnik", "Ploče", "Sibenik", "Makarska"));
        weatherPagerAdapter = new WeatherPagerAdapter(this, cities);

        // Set adapter to the ViewPager
        viewPager2.setAdapter(weatherPagerAdapter);

        // Add new city
        weatherPagerAdapter.addCity("Dubai");

        // Load saved cities
        loadSavedCities();

        // link local variables with UI layout elements
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
        // Adapter configuration
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        // RecyclerView configuration
        weather.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Set adapter to the RecyclerView
        weather.setAdapter(weatherAdapter);

        // Get location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        // Get last known location
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            // get city name
            citiesName = getCityName(location.getLongitude(), location.getLatitude());
            // add city to adapter
            weatherPagerAdapter.addCity(citiesName);
            getWeatherInfo(citiesName);
        } else {
            // add default city
            weatherPagerAdapter.addCity("Solin");
            // get weather info for Solin
            getWeatherInfo("Solin");
            Toast.makeText(MainActivity.this, "Please wait while loading...", Toast.LENGTH_SHORT).show();
        }

        // save updated list of cities
        searchIcon.setOnClickListener(v -> {
            // get city name
            String city = cityEdit.getText().toString().trim();
            if (!city.isEmpty()) {
                // add city to adapter
                weatherPagerAdapter.addCity(city);
                // save updated list of cities
                CitySharedPreferences.saveCities(this, (ArrayList<String>) weatherPagerAdapter.getCities());
                // get weather info for new city
                getWeatherInfo(city);
                // clear edit text
                cityEdit.setText("");
                hideKeyboard();
            } else {
                Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
            }
        });

        // load simulation of loading
        simulateLoading();
    }

    // hiding keyboard after entering city
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(cityEdit.getWindowToken(), 0);
        }
    }

    // loading simulation
    private void simulateLoading() {
        // set loading visibility
        loading.setVisibility(View.VISIBLE);
        // start loading
        new Handler().postDelayed(() -> loading.setVisibility(View.GONE), 3000);
    }

    // adding city to adapter
    public void addCity(String city) {
        weatherPagerAdapter.addCity(city);
        // save updated list of cities
        CitySharedPreferences.saveCities(this, (ArrayList<String>) weatherPagerAdapter.getCities());
        // get list of cities
        List<String> cities = weatherPagerAdapter.getCities();
        if (cities.size() >= 30)
            cities.remove(0);
        cities.add(city);
        // save updated list of cities
        viewPager2.setCurrentItem(weatherPagerAdapter.getItemCount() - 1, true);
    }

    // load saved cities
    private void loadSavedCities() {
        // get list of saved cities
        List<String> savedCities = CitySharedPreferences.getCities(this);

        if (savedCities != null) {
            for (String city : savedCities) {
                // add city to adapter
                weatherPagerAdapter.addCity(city);
            }
        }
    }

    // server response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // user permission check
        if (requestCode == PERMISSION_CODE) {
            // if permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                // close app
                finish();
            }
        }
    }

    // get city name
    private String getCityName(double longitude, double latitude) {
        String cityName = "Split";
        // geocoder for getting city name
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            // getting city location
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);
            assert addresses != null;
            for (Address address : addresses) {
                if (address != null) {
                    // getting city name
                    String city = address.getLocality();
                    if (city != null && !city.isEmpty()) {
                        // save city name
                        cityName = city;
                        break;
                    } else {
                        Toast.makeText(this, "City not found...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    // getting weather info data
    public void getWeatherInfo(String cityName) {
        String api_key = getString(R.string.weather_api_key);
        String api_key_url = "http://api.weatherapi.com/v1/forecast.json?key=" + api_key + "&q=" + cityName + "&days=1";

        // check if api key is empty
        if (api_key.isEmpty()) {
            Toast.makeText(this, "API key is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        // set city name
        city.setText(cityName);
        // create request queue data
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // create json object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, api_key_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loading.setVisibility(View.GONE);
                home.setVisibility(View.VISIBLE);
                weatherModelArrayList.clear();

                try {
                    // getting temperature data
                    JSONObject current = response.getJSONObject("current");
                    String t = current.getString("temp_c");
                    temperature.setText(t + "°C");

                    // getting condition data and icon
                    int isDay = current.getInt("is_day");
                    String condition = current.getJSONObject("condition").getString("text");
                    String conditionIcon = current.getJSONObject("condition").getString("icon");

                    // Picasso icon loading
                    Picasso.get()
                            .load("http:" + conditionIcon)
                            .error(R.drawable.weather_app)
                            .into(icon);

                    conditionView.setText(condition);

                    // Picasso background image loading
                    if (isDay == 1) {
                        // day
                        Picasso.get()
                                .load(R.drawable.weather_2)
                                .error(R.drawable.weather_1)
                                .into(background);
                    } else {
                        // night
                        Picasso.get()
                                .load(R.drawable.weather_3)
                                .error(R.drawable.weather_4)
                                .into(background);
                    }

                    // getting forecast data
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastdayObj = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    // display forecast card by hour
                    JSONArray hourArray = forecastdayObj.getJSONArray("hour");

                    // setting weather data in forecast card
                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        // adding data to weather model
                        weatherModelArrayList.add(new WeatherModel(time, temp, img, wind));
                    }
                    // updating weather adapter
                    weatherAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error processing the data...", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            // Error handling
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error fetching weather data. Try again later.", Toast.LENGTH_SHORT).show();
            }
        });
        // adding request to request queue
        requestQueue.add(jsonObjectRequest);
    }
}