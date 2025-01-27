package com.example.meteoweatherchannel;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class WeatherPagerAdapter extends FragmentStateAdapter {

    private final List<String> locations;

    public WeatherPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> locations) {
        super(fragmentActivity);
        this.locations = locations;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return WeatherFragment.newWeatherFragment(locations.get(position));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }
}
