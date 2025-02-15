package com.example.meteoweatherchannel;

public class Condition {
    private String text;
    private String icon;

    // Constructor
    public Condition(String text, String icon) {
        this.text = text;
        this.icon = icon;
    }

    // Getters and setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
