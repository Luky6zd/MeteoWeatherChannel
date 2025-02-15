package com.example.meteoweatherchannel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// adapter for weather items
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<WeatherModel> weatherModelArrayList;

    // constructor
    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModelArrayList) {
        this.context = context;
        this.weatherModelArrayList = weatherModelArrayList;
    }

    // create view holder for each weather item
    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item, parent, false);
        // return view holder
        return new ViewHolder(view);
    }

    // bind data to each item
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        // get weather model from array list
        WeatherModel weatherModel = weatherModelArrayList.get(position);
        // set data to views
        holder.temperature.setText(weatherModel.getTemperature() + "°C");
        holder.windSpeed.setText(weatherModel.getWindSpeed() + "Km/h");
        holder.time.setText(weatherModel.getTime() + "h");

        // getting weather icon from url with Picasso
        String iconUrl = weatherModel.getIcon();
        // if url is not missing, load icon
        if (iconUrl != null && !iconUrl.isEmpty()) {
            Picasso.get()
                    .load("http:" + iconUrl)
                    .error(R.drawable.cloudy)
                    .into(holder.condition);
        } else {
            // if url is missing, provided default icon
            Picasso.get()
                    .load("@drawable/cloudy.png")
                    .error(R.drawable.cloudy)
                    .into(holder.condition);
        }

        // Set wind speed
        String windSpeed = weatherModel.getWindSpeed();
        if (windSpeed != null && !windSpeed.isEmpty()) {
            holder.windSpeed.setText(windSpeed + " Km/h");
        } else {
            holder.windSpeed.setText("No data.");
        }

        // Set time
        String timeString = weatherModel.getTime();
        if (timeString != null && !timeString.isEmpty()) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());

            try {
                // Parse time string
                Date time = inputFormat.parse(timeString);
                if (time != null) {
                    holder.time.setText(outputFormat.format(time));
                }
            } catch (ParseException e) {
                e.printStackTrace();
                holder.time.setText("Invalid Time.");
            }
        } else {
            holder.time.setText("No data.");
        }
    }

    // size of weather array list
    @Override
    public int getItemCount() {
        return weatherModelArrayList.size();
    }

    // ViewHolder holds views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView windSpeed, temperature, time;
        private ImageView condition;

        // constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // link views with UI
            windSpeed = itemView.findViewById(R.id.windSpeed);
            temperature = itemView.findViewById(R.id.temperature);
            time = itemView.findViewById(R.id.time);
            condition = itemView.findViewById(R.id.condition);
        }
    }
}
