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
        // Inicijalizacija WeatherPagerAdaptera
        List<String> cities = new ArrayList<>(Arrays.asList("Zagreb", "Split", "Osijek", "Rijeka", "Karlovac", "Vukovar", "Zadar", "Pula"));
        weatherPagerAdapter = new WeatherPagerAdapter(this, cities);

        // Set adapter to the ViewPager
        viewPager2.setAdapter(weatherPagerAdapter);

        // Add a new city
        weatherPagerAdapter.addCity("Dubai");

        // Učitaj spremljene gradove
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

        // Get city name
        if (location != null) {
            // Dobivanje imena grada
            citiesName = getCityName(location.getLongitude(), location.getLatitude());
            // Dodaj taj grad u adapter
            weatherPagerAdapter.addCity(citiesName);
            // Spremi sve gradove
            getWeatherInfo(citiesName);
        } else {
            // Dodaj grad Zagreb u adapter
            weatherPagerAdapter.addCity("Zagreb");
            // Dobivanje weather infoa za Zagreb
            getWeatherInfo("Zagreb");
            Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }

        // Spremi ažuriranu listu gradova
        searchIcon.setOnClickListener(v -> {
            // Dobivanje imena grada
            String city = cityEdit.getText().toString().trim();
            if (!city.isEmpty()) {
                // Dodaj grad u adapter
                weatherPagerAdapter.addCity(city);
                // Spremi sve gradove
                CitySharedPreferences.saveCities(this, (ArrayList<String>) weatherPagerAdapter.getCities());
                // Dobivanje weather infoa za taj grad
                getWeatherInfo(city);
                // Očisti polje za pretragu
                cityEdit.setText("");
            } else {
                Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
            }
        });

        // Pokreni simulaciju učitavanja
        simulateLoading();

    }

    // simulacija učitavanja
    private void simulateLoading() {
        // Označi da se učitava
        loading.setVisibility(View.VISIBLE);
        // Pokreni simulaciju učitavanja
        new Handler().postDelayed(() -> loading.setVisibility(View.GONE), 3000);
    }

    // dodavanje grada
    public void addCity(String city) {
        // Dodaj grad u adapter
        weatherPagerAdapter.addCity(city);
        // Spremi sve gradove
        CitySharedPreferences.saveCities(this, (ArrayList<String>) weatherPagerAdapter.getCities());
        // dobavi listu gradova
        List<String> cities = weatherPagerAdapter.getCities();
        if (cities.size() >= 20)
            cities.remove(0);
        cities.add(city);
        // Spremi ažuriranu listu gradova
        viewPager2.setCurrentItem(weatherPagerAdapter.getItemCount() - 1, true);
    }

    // učitavanje spremljenih gradova
    private void loadSavedCities() {
        // Dobavljanje spremljenih gradova
        List<String> savedCities = CitySharedPreferences.getCities(this);

        if (savedCities != null) {
            for (String city : savedCities) {
                // Dodaj spremljene gradove u adapter
                weatherPagerAdapter.addCity(city);
            }
        }
    }

    // obrada odgovora sa servera
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Provjera da li korisnik ima dozvolu
        if (requestCode == PERMISSION_CODE) {
            // Ako korisnik prihvati dozvolu
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                // Zatvori aplikaciju
                finish();
            }
        }
    }

    // dobivanje imena grada
    private String getCityName(double longitude, double latitude) {
        String cityName = "Split";
        // Geocoder za dobivanje imena grada
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        try {
            // Dobivanje lokacije grada
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);
            assert addresses != null;
            for (Address address : addresses) {
                if (address != null) {
                    // Dobivanje imena grada
                    String city = address.getLocality();
                    if (city != null && !city.isEmpty()) {
                        // Spremanje imena grada
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

    // dobivanje weather info podataka
    public void getWeatherInfo(String cityName) {
        String api_key = getString(R.string.weather_api_key);
        String api_key_url = "http://api.weatherapi.com/v1/forecast.json?key=" + api_key + "&q=" + cityName + "&days=1";

        // Provjera da li je API ključ postavljen
        if (api_key.isEmpty()) {
            Toast.makeText(this, "API key is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        // Postavi grad
        city.setText(cityName);
        // Kreiranje novog zahtjeva
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        // Kreiranje zahtjeva za dobivanje odgovora se servera
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, api_key_url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loading.setVisibility(View.GONE);
                home.setVisibility(View.VISIBLE);
                weatherModelArrayList.clear();

                try {
                    // dobivanje podataka za temperaturu
                    JSONObject current = response.getJSONObject("current");
                    String t = current.getString("temp_c");
                    temperature.setText(t + "°C");

                    // dobivanje podataka za ikonu i opis
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
                                .load(R.drawable.weather_3)
                                .error(R.drawable.weather_7)
                                .into(background);
                    } else {
                        // night
                        Picasso.get()
                                .load(R.drawable.weather_8)
                                .error(R.drawable.weather_7)
                                .into(background);
                    }

                    // dobivanje podataka za vrijeme
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastdayObj = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    // prikaz weathera po satu (kartice)
                    JSONArray hourArray = forecastdayObj.getJSONArray("hour");

                    // Popunjavanje "hour arraya" weather podacima
                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        // Dodavanje podataka u weather model
                        weatherModelArrayList.add(new WeatherModel(time, temp, img, wind));
                    }
                    // Azuriranje adaptera
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
        // Dodavanje zahtjeva u red za izvršavanje
        requestQueue.add(jsonObjectRequest);
    }
}