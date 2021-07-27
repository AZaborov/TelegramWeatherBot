package com.telegramweatherbot.model;

import com.google.gson.annotations.SerializedName;

public class HourForecast {

    @SerializedName("DateTime")
    private String dateTime;
    @SerializedName("EpochDateTime")
    private Integer epochDateTime;
    @SerializedName("WeatherIcon")
    private Integer weatherIcon;
    @SerializedName("IconPhrase")
    private String iconPhrase;
    @SerializedName("HasPrecipitation")
    private Boolean hasPrecipitation;
    @SerializedName("IsDaylight")
    private Boolean isDaylight;
    @SerializedName("Temperature")
    private Temperature temperature;
    @SerializedName("PrecipitationProbability")
    private Integer precipitationProbability;
    @SerializedName("MobileLink")
    private String mobileLink;
    @SerializedName("Link")
    private String link;

    @SerializedName("DateTime")
    public String getDateTime() {
        return dateTime;
    }

    @SerializedName("DateTime")
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @SerializedName("EpochDateTime")
    public Integer getEpochDateTime() {
        return epochDateTime;
    }

    @SerializedName("EpochDateTime")
    public void setEpochDateTime(Integer epochDateTime) {
        this.epochDateTime = epochDateTime;
    }

    @SerializedName("WeatherIcon")
    public Integer getWeatherIcon() {
        return weatherIcon;
    }

    @SerializedName("WeatherIcon")
    public void setWeatherIcon(Integer weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    @SerializedName("IconPhrase")
    public String getIconPhrase() {
        return iconPhrase;
    }

    @SerializedName("IconPhrase")
    public void setIconPhrase(String iconPhrase) {
        this.iconPhrase = iconPhrase;
    }

    @SerializedName("HasPrecipitation")
    public Boolean getHasPrecipitation() {
        return hasPrecipitation;
    }

    @SerializedName("HasPrecipitation")
    public void setHasPrecipitation(Boolean hasPrecipitation) {
        this.hasPrecipitation = hasPrecipitation;
    }

    @SerializedName("IsDaylight")
    public Boolean getIsDaylight() {
        return isDaylight;
    }

    @SerializedName("IsDaylight")
    public void setIsDaylight(Boolean isDaylight) {
        this.isDaylight = isDaylight;
    }

    @SerializedName("Temperature")
    public Temperature getTemperature() {
        return temperature;
    }

    @SerializedName("Temperature")
    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    @SerializedName("PrecipitationProbability")
    public Integer getPrecipitationProbability() {
        return precipitationProbability;
    }

    @SerializedName("PrecipitationProbability")
    public void setPrecipitationProbability(Integer precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    @SerializedName("MobileLink")
    public String getMobileLink() {
        return mobileLink;
    }

    @SerializedName("MobileLink")
    public void setMobileLink(String mobileLink) {
        this.mobileLink = mobileLink;
    }

    @SerializedName("Link")
    public String getLink() {
        return link;
    }

    @SerializedName("Link")
    public void setLink(String link) {
        this.link = link;
    }
}
