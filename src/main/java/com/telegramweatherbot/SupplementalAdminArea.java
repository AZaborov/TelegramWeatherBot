package com.telegramweatherbot;

import com.google.gson.annotations.SerializedName;

public class SupplementalAdminArea {

    @SerializedName("Level")
    private Integer level;
    @SerializedName("LocalizedName")
    private String localizedName;
    @SerializedName("EnglishName")
    private String englishName;

    @SerializedName("Level")
    public Integer getLevel() {
        return level;
    }

    @SerializedName("Level")
    public void setLevel(Integer level) {
        this.level = level;
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
