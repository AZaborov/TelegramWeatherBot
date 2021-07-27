package com.telegramweatherbot.model;

import com.google.gson.annotations.SerializedName;

public class Country {

    @SerializedName("ID")
    private String id;
    @SerializedName("LocalizedName")
    private String localizedName;
    @SerializedName("EnglishName")
    private String englishName;

    @SerializedName("ID")
    public String getId() {
        return id;
    }

    @SerializedName("ID")
    public void setId(String id) {
        this.id = id;
    }

    @SerializedName("LocalizedName")
    public String getLocalizedName() {
        return localizedName;
    }

    @SerializedName("LocalizedName")
    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    @SerializedName("EnglishName")
    public String getEnglishName() {
        return englishName;
    }

    @SerializedName("EnglishName")
    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

}
