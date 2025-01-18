package com.example.meteoweatherchannel;

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

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private Context context;
    private ArrayList<WeatherModel> weatherModelArrayList;

    // constructor
    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModelArrayList) {
        this.context = context;
        this.weatherModelArrayList = weatherModelArrayList;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {

        WeatherModel weatherModel = weatherModelArrayList.get(position);

        holder.temperature.setText(weatherModel.getTemperature() + "°C");

        String iconUrl = weatherModel.getIcon();

        // getting weather icon from Picasso
        if (iconUrl != null && !iconUrl.isEmpty()) {
            Picasso.get()
                    .load("http:" + iconUrl)
                    .into(holder.condition);
        } else {
            // if url is missing, provided default icon
            Picasso.get()
                    .load("@drawable/cloudy.png")
                    .into(holder.condition);
        }

        String windSpeed = weatherModel.getWindSpeed();
        if (windSpeed != null && !windSpeed.isEmpty()) {
            holder.windSpeed.setText(windSpeed + " Km/h");
        } else {
            holder.windSpeed.setText("No data.");
        }

        String timeString = weatherModel.getTime();
        if (timeString != null && !timeString.isEmpty()) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());

            try {
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

    @Override
    public int getItemCount() {
        return weatherModelArrayList.size();
    }

    // ViewHolder class that holds views for each item
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView windSpeed, temperature, time;
        private ImageView condition;

        // constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            windSpeed = itemView.findViewById(R.id.windSpeed);
            temperature = itemView.findViewById(R.id.temperature);
            time = itemView.findViewById(R.id.time);
            condition = itemView.findViewById(R.id.condition);
        }
    }
}
