package com.telegramweatherbot.model;

import com.google.gson.annotations.SerializedName;
import com.telegramweatherbot.service.Utils;

public class TimeZone {

    @SerializedName("Code")
    private String code;
    @SerializedName("Name")
    private String name;
    @SerializedName("GmtOffset")
    private Double gmtOffset;
    @SerializedName("IsDaylightSaving")
    private Boolean isDaylightSaving;
    @SerializedName("NextOffsetChange")
    private String nextOffsetChange;

    @SerializedName("Code")
    public String getCode() {
        return code;
    }

    @SerializedName("Code")
    public void setCode(String code) {
        this.code = code;
    }

    @SerializedName("Name")
    public String getName() {
        return Utils.getUtils().formatTimeZone(name);
    }

    @SerializedName("Name")
    public void setName(String name) {
        this.name = name;
    }

    @SerializedName("GmtOffset")
    public Double getGmtOffset() {
        return gmtOffset;
    }

    @SerializedName("GmtOffset")
    public void setGmtOffset(Double gmtOffset) {
        this.gmtOffset = gmtOffset;
    }

    @SerializedName("IsDaylightSaving")
    public Boolean getIsDaylightSaving() {
        return isDaylightSaving;
    }

    @SerializedName("IsDaylightSaving")
    public void setIsDaylightSaving(Boolean isDaylightSaving) {
        this.isDaylightSaving = isDaylightSaving;
    }

    @SerializedName("NextOffsetChange")
    public String getNextOffsetChange() {
        return nextOffsetChange;
    }

    @SerializedName("NextOffsetChange")
    public void setNextOffsetChange(String nextOffsetChange) {
        this.nextOffsetChange = nextOffsetChange;
    }

}
