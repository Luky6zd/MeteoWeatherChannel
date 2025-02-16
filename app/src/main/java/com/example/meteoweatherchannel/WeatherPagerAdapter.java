package com.example.meteoweatherchannel;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

//pager adapter for weather fragments and cities
public class WeatherPagerAdapter extends FragmentStateAdapter {
    private List<String> cities;

    // Constructor
    public WeatherPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> cities) {
        super(fragmentActivity);
        this.cities = cities;
    }

    // add city
    public void addCity(String city) {
        // Prevent duplicates
        if (!cities.contains(city)) {
            cities.add(city);
            notifyItemInserted(cities.size() - 1);

            // set max limit of 30 cities
            if (cities.size() > 30) {
                cities.remove(0);
                notifyItemRemoved(0);
                notifyItemRangeChanged(0, cities.size()); // Update remaining cities
            }
        }
    }

    // get all cities
    public List<String> getCities() {
        // return copy of cities list
        return new ArrayList<>(cities);
    }

    // create fragment for each city
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // return weather fragment for city
        return WeatherFragment.newInstance(cities.get(position));
    }

    // get number of cities
    @Override
    public int getItemCount() {
        return cities.size();
    }
}
