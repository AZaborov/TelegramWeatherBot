package com.telegramweatherbot;

import com.google.gson.annotations.SerializedName;

public class Elevation {

    @SerializedName("Metric")
    private Metric metric;
    @SerializedName("Imperial")
    private Imperial imperial;

    @SerializedName("Metric")
    public Metric getMetric() {
        return metric;
    }

    @SerializedName("Metric")
    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    @SerializedName("Imperial")
    public Imperial getImperial() {
        return imperial;
    }

    @SerializedName("Imperial")
    public void setImperial(Imperial imperial) {
        this.imperial = imperial;
    }

}
