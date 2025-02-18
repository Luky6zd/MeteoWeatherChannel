package com.example.meteoweatherchannel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout home;
    private ProgressBar loading;
    private TextView city, temperature, conditionView;
    private TextInputEditText cityEdit;
    private ImageView background, icon, searchIcon;
    private RecyclerView weather;
    private ArrayList<WeatherModel> weatherModelArrayList;
    private WeatherAdapter weatherAdapter;
    private WeatherPagerAdapter weatherPagerAdapter;
    private LocationManager locationManager;
    private final int PERMISSION_CODE = 1;
    private String citiesName;
    private ViewPager2 viewPager2;
    private SharedPreferences prefs;

    // location variables
    private final String[] LOCATION_PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // permission launcher variable
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                // permission check result
                public void onActivityResult(Map<String, Boolean> result) {
                    if (Boolean.TRUE.equals(result.get(LOCATION_PERMISSIONS[0]))) {
                        // if permission is granted
                        startLocationUpdates();
                    } else {
                        if (shouldShowRequestPermissionRationale(LOCATION_PERMISSIONS[0])) {
                            showPermissionDialog();
                        } else {
                            Toast.makeText(MainActivity.this, "Please enable location permissions", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
            }
    );

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // app on full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        // link local variables with UI layout components
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

        // Initialize RecyclerView and Adapter
        weatherModelArrayList = new ArrayList<>();
        // Adapter configuration
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        // RecyclerView configuration
        weather.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Set adapter to the RecyclerView
        weather.setAdapter(weatherAdapter);

        // Initialize ViewPager2 and Adapter
        viewPager2 = findViewById(R.id.viewPager);
        List<String> cities = new ArrayList<>(Arrays.asList("Solin", "Split", "Zagreb", "Osijek", "Rijeka", "Karlovac", "Vukovar", "Zadar", "Pula", "Dubrovnik", "Ploče", "Sibenik", "Makarska"));
        weatherPagerAdapter = new WeatherPagerAdapter(this, cities);
        // Set adapter to the ViewPager
        viewPager2.setAdapter(weatherPagerAdapter);

        // Initialize SharedPreferences
        prefs = getSharedPreferences("weather_prefs", MODE_PRIVATE);

        // Get location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Location Services
        locationRequest = new LocationRequest.Builder(1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(10)
                .build();

        // Initialize Location Callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    String cityName = getCityName(location.getLongitude(), location.getLatitude());
                    weatherPagerAdapter.addCity(cityName);
                    getWeatherInfo(cityName);
                } else {
                    weatherPagerAdapter.addCity("Solin");
                    getWeatherInfo("Solin");
                    Toast.makeText(MainActivity.this, "Please wait while loading...", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Check and request location permissions
        checkAndRequestPermissions();

        // Set up search icon
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

        // Simulate loading
        simulateLoading();

        // Load saved cities
        loadSavedCities();
    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // check if GPS is enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // get location client
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            // check location permissions
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }

                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }
                }).addOnSuccessListener(this, loc -> {
                    if (loc != null) {
                        double latitude = loc.getLatitude();
                        double longitude = loc.getLongitude();
                        String city = getCityName(longitude, latitude);
                        getWeatherInfo(city);
                    } else {
                        // Fallback on default city when location is not available
                        getWeatherInfo("Split");
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // if GPS is not enabled
                Toast.makeText(this, "Turn on GPS for location", Toast.LENGTH_LONG).show();
            }
        }
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
        // scroll for new city
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

    // permission check result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // user permission check
        if (requestCode == PERMISSION_CODE) {
            // if permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // get location access
                getCurrentLocation();
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                // close app
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String city = "Split";
        // geocoder for getting city name
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            // try getting city location
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                return "Split";
            }

            for (Address address : addresses) {
                if (address != null && !addresses.isEmpty()) {
                    // getting city name
                    city = addresses.get(0).getLocality();

                    if (city != null && !city.isEmpty()) {
                        // save city name
                        city = city;
                        break;
                    } else {
                        Toast.makeText(this, "City not found...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return city;
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

    // check and request permissions
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, LOCATION_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
            // if permission is granted get last known location
            Location location = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);

            if (location != null) {
                // if location exists get city name
                citiesName = getCityName(location.getLongitude(), location.getLatitude());
                // add city to adapter
                weatherPagerAdapter.addCity(citiesName);
                getWeatherInfo(citiesName);
            } else {
                // add default city
                weatherPagerAdapter.addCity("Solin");
                // Fallback on default city
                getWeatherInfo("Solin");
                Toast.makeText(MainActivity.this, "Please wait while loading...", Toast.LENGTH_SHORT).show();
            }
            startLocationUpdates();
        } else {
            // when permission is not granted
            requestPermissionLauncher.launch(LOCATION_PERMISSIONS);
        }
    }

    // if permission is not granted, show dialog
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location permission needed!")
                .setMessage("Can't determine location without permission")
                .setPositiveButton("I understand", (dialog, which) -> {
                    requestPermissionLauncher.launch(LOCATION_PERMISSIONS);
                    dialog.dismiss();
                });

                // open GPS settings
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

        AlertDialog alertDialog = builder.create();
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    // if permission is granted, start location updates
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    // remove when app is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}