package com.example.meteoweatherchannel;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

// adapter for weather data in weather fragment
public class WeatherFragmentAdapter extends RecyclerView.Adapter<WeatherFragmentAdapter.WeatherViewHolder> {

    private List<WeatherData> weatherList;

    // Constructor
    public WeatherFragmentAdapter(List<WeatherData> weatherList) {
        this.weatherList = weatherList;
    }

    // ViewHolder class
    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView, temperatureTextView, windSpeedTextView;
        ImageView iconImageView;

        // Constructor
        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            timeTextView = itemView.findViewById(R.id.time);
            temperatureTextView = itemView.findViewById(R.id.temperature);
            iconImageView = itemView.findViewById(R.id.icon);
            windSpeedTextView = itemView.findViewById(R.id.windSpeed);
        }
    }

    // Create new ViewHolder
    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item, parent, false);

        return new WeatherViewHolder(view);
    }

    // Bind data to ViewHolder
    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        // Bind data to the views
        WeatherData weather = weatherList.get(position);
        // Set data to the views
        holder.timeTextView.setText("Time: " + weather.getTime());
        holder.temperatureTextView.setText("Temperature: " + weather.getTemperature());
        holder.windSpeedTextView.setText("Wind Speed: " + weather.getWindSpeed());

        Picasso.get()
                .load(weather.getIcon())
                .error(R.drawable.cloudy)
                .into(holder.iconImageView);
    }

    // Get number of weather items
    @Override
    public int getItemCount() {
        // Returns total number of weather fragments (locations)
        return weatherList.size();
    }

    // update list of weather data
    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<WeatherData> newList) {
        weatherList = newList;
        notifyDataSetChanged();
    }
}