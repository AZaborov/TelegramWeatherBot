package com.telegramweatherbot;

import com.google.gson.annotations.SerializedName;

public class GeoPosition {

    @SerializedName("Latitude")
    private Double latitude;
    @SerializedName("Longitude")
    private Double longitude;
    @SerializedName("Elevation")
    private Elevation elevation;

    @SerializedName("Latitude")
    public Double getLatitude() {
        return latitude;
    }

    @SerializedName("Latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @SerializedName("Longitude")
    public Double getLongitude() {
        return longitude;
    }

    @SerializedName("Longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @SerializedName("Elevation")
    public Elevation getElevation() {
        return elevation;
    }

    @SerializedName("Elevation")
    public void setElevation(Elevation elevation) {
        this.elevation = elevation;
    }

}
