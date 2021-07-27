package com.telegramweatherbot.service;

import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HTTPRequest {

    private static final Logger logger = Logger.getLogger(HTTPRequest.class);
    private static final String accuWeatherApiKey = "m5gjz8PVZFPS95FwB9D6oZEK2X6cZSDo";
    private static final String forecast12HourRequest = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/";
    private static final String citiesRequest = "http://dataservice.accuweather.com/locations/v1/cities/search";
    private static final String locationRequest = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search";

    private HTTPRequest(){}

    public static String get12HourForecast(String locationCode) {
        logger.debug("Пользователь запросил прогноз погоды по AccuWeather API");
        String url = forecast12HourRequest + locationCode + "?apikey=" + accuWeatherApiKey + "&language=ru-ru&metric=true";
        return getUrlContents(url);
    }

    public static String getCities(String cityName) {
        logger.debug("Пользователь запросил список городов по AccuWeather API");
        String url = citiesRequest + "?apikey=" + accuWeatherApiKey + "&q=" + cityName;
        return getUrlContents(url);
    }

    public static String getLocation(Float latitude, Float longitude) {
        logger.debug("Пользователь запросил информацию о локации по AccuWeather API");
        String url = locationRequest + "?apikey=" + accuWeatherApiKey + "&q=" + latitude + "%2C%20" + longitude;
        return getUrlContents(url);
    }

    private static String getUrlContents(String theUrl) {
        logger.debug("Программа в методе получения данных по http запросу");

        StringBuilder content = new StringBuilder();
        try {
            logger.warn("Возможна ошибка получения данных из-за неверно введённого адреса");
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
            logger.error("Программе не удалось получить данные из полученной ссылки");
            e.printStackTrace();
        }

        return content.toString();
    }

}
