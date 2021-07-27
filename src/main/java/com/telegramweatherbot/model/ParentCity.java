package com.telegramweatherbot.model;

import com.google.gson.annotations.SerializedName;

public class ParentCity {

    @SerializedName("Key")
    private String key;
    @SerializedName("LocalizedName")
    private String localizedName;
    @SerializedName("EnglishName")
    private String englishName;

    @SerializedName("Key")
    public String getKey() {
        return key;
    }

    @SerializedName("Key")
    public void setKey(String key) {
        this.key = key;
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
