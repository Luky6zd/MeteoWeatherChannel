package com.example.meteoweatherchannel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class SearchFragment extends Fragment {
    private EditText editTextCity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout for search fragment
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        editTextCity = view.findViewById(R.id.editTextCity);

        // Set click listener for search button
        view.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            // Get city name from EditText
            String city = editTextCity.getText().toString();
            if (!city.isEmpty()) {
                // Add city to the adapter
                ((MainActivity) requireActivity()).addCity(city);
            }
        });

        return view;
    }
}
