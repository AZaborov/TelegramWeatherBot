package com.telegramweatherbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HTTPRequest {

    private static final String accuWeatherApiKey = "m5gjz8PVZFPS95FwB9D6oZEK2X6cZSDo";
    private static final String forecast12HourRequest = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/";
    private static final String citiesRequest = "http://dataservice.accuweather.com/locations/v1/cities/search";
    private static final String locationRequest = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search";

    private HTTPRequest(){}

    public static String get12HourForecast(String locationCode) {
        String url = forecast12HourRequest + locationCode + "?apikey=" + accuWeatherApiKey + "&language=ru-ru&metric=true";
        return getUrlContents(url);
    }

    public static String getCities(String cityName) {
        String url = citiesRequest + "?apikey=" + accuWeatherApiKey + "&q=" + cityName;
        return getUrlContents(url);
    }

    public static String getLocation(Float latitude, Float longitude) {
        String url = locationRequest + "?apikey=" + accuWeatherApiKey + "&q=" + latitude + "%2C%20" + longitude;
        return getUrlContents(url);
    }

    private static String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }
            bufferedReader.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return content.toString();
    }

}
