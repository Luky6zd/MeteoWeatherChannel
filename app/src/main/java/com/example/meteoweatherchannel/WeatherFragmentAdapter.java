package com.example.meteoweatherchannel;

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

public class WeatherFragmentAdapter extends RecyclerView.Adapter<WeatherFragmentAdapter.WeatherViewHolder> {

    // List of weather data
    private List<WeatherData> weatherList;

    // Constructor
    public WeatherFragmentAdapter(List<WeatherData> weatherList) {
        this.weatherList = weatherList;
    }

    public WeatherFragmentAdapter(FragmentManager supportFragmentManager, List<Fragment> fragmentList) {
    }

    // ViewHolder class
    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView, temperatureTextView, windSpeedTextView;
        ImageView iconImageView;

        // Constructor
        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.time);
            temperatureTextView = itemView.findViewById(R.id.temperature);
            iconImageView = itemView.findViewById(R.id.icon);
            windSpeedTextView = itemView.findViewById(R.id.windSpeed);
        }
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_item, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        // Bind data to the views
        WeatherData weather = weatherList.get(position);
        // Set data to the views
        holder.timeTextView.setText("Time: " + weather.getTime());
        holder.temperatureTextView.setText("Temperature: " + weather.getTemperature());
        //holder.iconImageView.setImageDrawable("Icon: " + weather.getIcon());
        Picasso.get()
                .load(weather.getIcon())
                .into(holder.iconImageView);

        holder.windSpeedTextView.setText("Wind Speed: " + weather.getWindSpeed());
    }

    // Get the number of items in the list
    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    // Method to update the list
    public void updateList(List<WeatherData> newList) {
        weatherList = newList;
        notifyDataSetChanged();
    }
}