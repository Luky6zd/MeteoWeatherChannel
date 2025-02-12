package com.example.meteoweatherchannel;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class WeatherPagerAdapter extends FragmentStateAdapter {
    // Lista za pohranu gradova
    private List<String> cities;

    public WeatherPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> cities) {
        super(fragmentActivity);
        this.cities = cities;
    }

    public WeatherPagerAdapter(MainActivity weatherActivity) {
        super(weatherActivity);
    }

    public void addCity(String city) {
        // Spriječi duplikate
        if (!cities.contains(city)) {
            cities.add(city);
            notifyItemInserted(cities.size() - 1);

            // Ograniči na 10 gradova
            if (cities.size() > 10) {
                cities.remove(0);
                notifyItemRangeChanged(0, cities.size());
            }
        } else {
            // Ako grad već postoji, ažuriraj fragment
            int index = cities.indexOf(city);
            cities.set(index, city);
            notifyItemChanged(index);
        }
    }

    // Metoda za dobivanje svih gradova
    public List<String> getCities() {
        // Vraća kopiju liste
        return new ArrayList<>(cities);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Kreiraj fragment za grad
        return WeatherFragment.newInstance(cities.get(position));
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }
}
