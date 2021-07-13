package com.telegramweatherbot;

import com.google.gson.annotations.SerializedName;

public class Temperature {
    @SerializedName("Value")
    private Double value;
    @SerializedName("Unit")
    private String unit;
    @SerializedName("UnitType")
    private Integer unitType;

    @SerializedName("Value")
    public Double getValue() {
        return value;
    }

    @SerializedName("Value")
    public void setValue(Double value) {
        this.value = value;
    }

    @SerializedName("Unit")
    public String getUnit() {
        return unit;
    }

    @SerializedName("Unit")
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @SerializedName("UnitType")
    public Integer getUnitType() {
        return unitType;
    }

    @SerializedName("UnitType")
    public void setUnitType(Integer unitType) {
        this.unitType = unitType;
    }
}
