package com.telegramweatherbot.service;

import com.google.gson.Gson;
import com.telegramweatherbot.model.HourForecast;
import org.apache.commons.text.WordUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;

public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class);
    private static Properties properties;
    private static Utils utils;

    public Utils(){}

    public static synchronized Utils getUtils() {
        logger.debug("Программа в методе getUtils()");

        if (utils == null) {
            utils = new Utils();
        }

        return utils;
    }

    public static Properties getProperties() {
        return properties;
    }

    public String formatHistory(ArrayList<String> history) {
        logger.debug("Программа в методе formatHistory()");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < history.size(); i++) {
            sb.append(i + 1);
            sb.append(") ");
            String name = WordUtils.capitalizeFully(history.get(i).toLowerCase());
            sb.append(name);
            sb.append("\n");
        }

        return sb.toString();
    }

    public String formatTimeZone(String timeZone) {
        timeZone = timeZone.toLowerCase();
        String[] dummy = timeZone.split("/");
        return WordUtils.capitalize(dummy[0]) + "/" + WordUtils.capitalize(dummy[1]);
    }

    public String getUrlContents(String theUrl) {
        logger.debug("Программа в методе getUrlContents()");

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
            logger.error(e.getMessage(), e);
        }

        return content.toString();
    }

    public void readConfigFile() {
        logger.debug("Программа в методе readConfigFile()");
        properties = new Properties();

        try {
            InputStream inputStream = TelegramWeatherBot.class.getResourceAsStream("/config.properties");
            properties.load(inputStream);
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getForecastMessage(String code, String prevMessage) {
        logger.debug("Программа в методе getForecastMessage()");

        String contents = AccuWeatherRequests.getRequests().get12HourForecast(code);
        Gson gson = new Gson();
        HourForecast[] hourForecasts = gson.fromJson(contents, HourForecast[].class);
        logger.debug("Программа получила json сообщение с прогнозом погоды и распрасила его");

        StringBuilder message = new StringBuilder(prevMessage);
        message.append("\n\nПрогноз на ближайшие 12 часов:\n");

        for (HourForecast f : hourForecasts) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
            LocalTime time = LocalTime.parse(f.getDateTime(), formatter);
            message.append(time);
            message.append(" ");
            message.append(f.getTemperature().getValue());
            message.append("°");
            message.append(f.getTemperature().getUnit());
            message.append(" ");
            message.append(f.getIconPhrase());
            message.append("\n");
        }
        logger.debug("Программа составила сообщение с прогнозом погоды");

        return message.toString();
    }
}
