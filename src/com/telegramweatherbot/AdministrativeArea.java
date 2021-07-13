package com.telegramweatherbot;

import com.google.gson.annotations.SerializedName;

public class AdministrativeArea {

    @SerializedName("ID")
    private String id;
    @SerializedName("LocalizedName")
    private String localizedName;
    @SerializedName("EnglishName")
    private String englishName;
    @SerializedName("Level")
    private Integer level;
    @SerializedName("LocalizedType")
    private String localizedType;
    @SerializedName("EnglishType")
    private String englishType;
    @SerializedName("CountryID")
    private String countryID;

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

    @SerializedName("Level")
    public Integer getLevel() {
        return level;
    }

    @SerializedName("Level")
    public void setLevel(Integer level) {
        this.level = level;
    }

    @SerializedName("LocalizedType")
    public String getLocalizedType() {
        return localizedType;
    }

    @SerializedName("LocalizedType")
    public void setLocalizedType(String localizedType) {
        this.localizedType = localizedType;
    }

    @SerializedName("EnglishType")
    public String getEnglishType() {
        return englishType;
    }

    @SerializedName("EnglishType")
    public void setEnglishType(String englishType) {
        this.englishType = englishType;
    }

    @SerializedName("CountryID")
    public String getCountryID() {
        return countryID;
    }

    @SerializedName("CountryID")
    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

}
