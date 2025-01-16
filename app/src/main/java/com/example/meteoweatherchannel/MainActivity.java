package com.example.meteoweatherchannel;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.WeakHashMap;

public class MainActivity extends AppCompatActivity {

    // initializing variables
    private RelativeLayout home;
    private ProgressBar loading;
    private TextView cityName, temperature, condition;
    private TextInputEditText cityEdit;
    private ImageView background, icon, searchIcon;
    private RecyclerView weather;
    private ArrayList<WeatherModel> weatherModelArrayList;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // make app on full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        // linking local variables with layout by its ID (defined in View)
        // findViewById returns a reference to the view
        home = findViewById(R.id.home);
        loading = findViewById(R.id.progressBarLoading);
        cityName = findViewById(R.id.cityName);
        temperature = findViewById(R.id.temperature);
        condition = findViewById(R.id.condition);
        cityEdit = findViewById(R.id.editCity);
        background = findViewById(R.id.background);
        icon = findViewById(R.id.icon);
        searchIcon = findViewById(R.id.searchIcon);
        weather = findViewById(R.id.weather);

        weatherModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherModelArrayList);
        weather.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    private void getWeatherInfo(String cityName) {

        String url = "http://api.weatherapi.com/v1/forecast.json?key=5fd2073805c944a1a55165032251601&q=" + cityName + "&days=1&aqi=no&alerts=no";
    }
}