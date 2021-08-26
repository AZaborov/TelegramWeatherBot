package com.telegramweatherbot.service;

import org.apache.log4j.Logger;

public class AccuWeatherRequests {

    private static final Logger logger = Logger.getLogger(AccuWeatherRequests.class);
    private static final String forecast12HourRequest = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/";
    private static final String citiesRequest = "http://dataservice.accuweather.com/locations/v1/cities/search";
    private static final String locationRequest = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search";
    private static AccuWeatherRequests requests;
    private final String accuWeatherApiKey;

    public AccuWeatherRequests() {
        logger.debug("Программа в конструкторе класса AccuWeatherRequests");
        accuWeatherApiKey = Utils.getProperties().getProperty("accuWeatherApiKey");
    }

    public static synchronized AccuWeatherRequests getRequests() {
        logger.debug("Программа в методе getRequests() класса AccuWeatherRequests");

        if (requests == null) {
            requests = new AccuWeatherRequests();
        }

        return requests;
    }

    public String get12HourForecast(String locationCode) {
        logger.debug("Программа в методе get12HourForecast() класса AccuWeatherRequests");

        StringBuilder url;
        url = new StringBuilder();
        url.append(forecast12HourRequest);
        url.append(locationCode);
        url.append("?apikey=");
        url.append(accuWeatherApiKey);
        url.append("&language=ru-ru&metric=true");

        return Utils.getUtils().getUrlContents(url.toString());
    }

    public String getCities(String cityName) {
        logger.debug("Программа в методе getCities() класса AccuWeatherRequests");

        StringBuilder url;
        url = new StringBuilder();
        url.append(citiesRequest);
        url.append("?apikey=");
        url.append(accuWeatherApiKey);
        url.append("&q=");
        url.append(cityName);

        return Utils.getUtils().getUrlContents(url.toString());
    }

    public String getLocation(Float latitude, Float longitude) {
        logger.debug("Программа в методе getLocation() класса AccuWeatherRequests");

        StringBuilder url;
        url = new StringBuilder();
        url.append(locationRequest);
        url.append("?apikey=");
        url.append(accuWeatherApiKey);
        url.append("&q=");
        url.append(latitude);
        url.append("%2C%20");
        url.append(longitude);

        return Utils.getUtils().getUrlContents(url.toString());
    }
}
