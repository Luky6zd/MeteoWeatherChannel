package com.example.meteoweatherchannel;

public class Location {
    private String locationName;

    // Constructor
    public Location(String locationName) {
        this.locationName = locationName;
    }

    // Getter and setter
    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
