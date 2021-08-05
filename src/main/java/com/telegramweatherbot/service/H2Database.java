package com.telegramweatherbot.service;

import com.telegramweatherbot.model.LocationByCity;
import com.telegramweatherbot.model.TimeZone;
import org.apache.commons.text.WordUtils;
import org.apache.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.Locale;

public class H2Database {

    private static final Logger logger = Logger.getLogger(H2Database.class);
    private static final String url      = "jdbc:h2:~/weatherdb";
    private static final String user     = "sa";
    private static final String password = "";
    private static Statement statement;

    private H2Database(){}

    public static void createTables() throws ClassNotFoundException {
        logger.debug("Программа в методе createTables()");

        Class.forName("org.h2.Driver");
        logger.info("Подключена база данных");

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            statement.execute("DROP TABLE IF EXISTS CHATS_FLAGS");
            statement.execute("DROP TABLE IF EXISTS CITY_REQUESTS");
            statement.execute("DROP TABLE IF EXISTS GEO_REQUESTS");
            statement.execute("DROP TABLE IF EXISTS CITIES");
            statement.execute("DROP TABLE IF EXISTS DAILY_FORECAST_SETTINGS");

            StringBuilder sqlChats = new StringBuilder("CREATE TABLE CHATS_FLAGS");
            sqlChats.append("(CHAT_ID BIGINT PRIMARY KEY");
            sqlChats.append(", DAILY_FORECAST_TIME_INPUT_ON BIT");
            sqlChats.append(", DAILY_FORECAST_CITY_INPUT_ON BIT");
            sqlChats.append(", CITY_NAME_INPUT_ON BIT");
            sqlChats.append(", CITY_NUMBER_CHOOSE_ON BIT");
            sqlChats.append(", LOCATION_INPUT_ON BIT");
            sqlChats.append(", CITY_HISTORY_CHOOSE_ON BIT");
            sqlChats.append(", GEO_HISTORY_CHOOSE_ON BIT)");
            statement.execute(sqlChats.toString());
            
            StringBuilder sqlDailyForecastSettings = new StringBuilder("CREATE TABLE DAILY_FORECAST_SETTINGS");
            sqlDailyForecastSettings.append("(CHAT_ID BIGINT PRIMARY KEY");
            sqlDailyForecastSettings.append(", DAILY_FORECAST_TIME TIME");
            sqlDailyForecastSettings.append(", DAILY_FORECAST_CITY_NAME VARCHAR(100)");
            sqlDailyForecastSettings.append(", DAILY_FORECAST_CITY_CODE INT");
            sqlDailyForecastSettings.append(", DAILY_FORECAST_TIME_ZONE VARCHAR(100)");
            sqlDailyForecastSettings.append(", DAILY_FORECAST_ON BIT)");
            statement.execute(sqlDailyForecastSettings.toString());
            
            statement.execute("CREATE TABLE CITY_REQUESTS(CHAT_ID BIGINT, CITY_CODE INT, CITY_NAME VARCHAR(100))");
            statement.execute("CREATE TABLE GEO_REQUESTS(CHAT_ID BIGINT, GEO_CODE INT, GEO_NAME VARCHAR(100))");
            statement.execute("CREATE TABLE CITIES(CHAT_ID BIGINT, CITY_CODE INT, CITY_NAME VARCHAR(100), CITY_TIME_ZONE VARCHAR(100))");

            logger.info("В базе данных созданы таблицы");
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void addChat(long id) {
        logger.debug("Программа в методе addChat()");

        try {
            statement.execute(String.format("INSERT INTO CHATS_FLAGS VALUES (%S, 0, 0, 0, 0, 0, 0, 0)", id));
            statement.execute(String.format("INSERT INTO DAILY_FORECAST_SETTINGS VALUES (%S, '09:00:00', 'Moscow, Russia, Moscow', 294021, 'Europe/Moscow', 0)", id));
            logger.info(String.format("В базу данных добавлен новый чат (id %s)", id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static Boolean chatExists(long id) {
        logger.debug("Программа в методе chatExists()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT * FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            return result.next();
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static void addCities(long id, LocationByCity[] cities) {
        logger.debug("Программа в методе addCities()");

        try {
            statement.execute(String.format("DELETE FROM CITIES WHERE CHAT_ID='%S'", id));

            for (LocationByCity city : cities) {

                StringBuilder cityName = new StringBuilder();
                cityName.append(city.getLocalizedName());
                cityName.append(", ");
                cityName.append(city.getCountry().getLocalizedName());
                cityName.append(", ");
                cityName.append(city.getAdministrativeArea().getLocalizedName());

                // Если в названии города есть апостроф, его нужно удалить, чтобы SQL запрос не сломался
                while (cityName.toString().contains("'")) {
                    int j = cityName.indexOf("'");
                    cityName.replace(j, j+1, "");
                }

                String timeZone = city.getTimeZone().getName();
                statement.execute(String.format("INSERT INTO CITIES VALUES (%S, %S, '%S', '%S')", id, city.getKey(), cityName, timeZone));
            }

            logger.info(String.format("В базу данных добавлены результаты запроса городов для чата %s", id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static ArrayList<LocationByCity> getCities(long id) {
        logger.debug("Программа в методе getCities()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT * FROM CITIES WHERE CHAT_ID='%S'", id));
            ArrayList<LocationByCity> cities = new ArrayList<>();

            while (result.next()) {
                LocationByCity city = new LocationByCity();
                city.setLocalizedName(WordUtils.capitalizeFully(result.getString("CITY_NAME").toLowerCase()));
                city.setKey(result.getString("CITY_CODE"));
                TimeZone timeZone = new TimeZone();
                timeZone.setName(result.getString("CITY_TIME_ZONE"));
                city.setTimeZone(timeZone);
                cities.add(city);
            }
            return cities;
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    //---------------------------------------------Ежедневный прогноз---------------------------------------------------
    public static Time getDailyForecastTime(long id) {
        logger.debug("Программа в методе getDailyForecastTime()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_TIME FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getTime(1);
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return Time.valueOf("09:00:00");
    }

    public static void setDailyForecastTime(long id, Time time) {
        logger.debug("Программа в методе setDailyForecastTime()");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_TIME='%S' WHERE CHAT_ID='%S'", time, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getDailyForecastCityName(long id) {
        logger.debug("Программа в методе getDailyForecastCityName()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_CITY_NAME FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return WordUtils.capitalizeFully(result.getString(1).toLowerCase());
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "Moscow, Russia, Moscow";
    }

    public static void setDailyForecastCityName(long id, String name) {
        logger.debug("Программа в методе setDailyForecastCityName()");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_CITY_NAME='%S' WHERE CHAT_ID='%S'", name, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getDailyForecastCityCode(long id) {
        logger.debug("Программа в методе getDailyForecastCityCode()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_CITY_CODE FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getString("DAILY_FORECAST_CITY_CODE");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "294021";
    }

    public static void setDailyForecastCityCode(long id, String code) {
        logger.debug("Программа в методе setDailyForecastCityCode()");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_CITY_CODE=%S WHERE CHAT_ID='%S'", code, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getDailyForecastTimeZone(long id) {
        logger.debug("Программа в методе getDailyForecastTimeZone()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_TIME_ZONE FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                String rawTimeZone = result.getString("DAILY_FORECAST_TIME_ZONE");
                rawTimeZone = rawTimeZone.toLowerCase();
                String[] dummy = rawTimeZone.split("/");

                return WordUtils.capitalize(dummy[0]) + "/" + WordUtils.capitalize(dummy[1]);
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "Europe/Moscow";
    }

    public static void setDailyForecastTimeZone(long id, String zone) {
        logger.debug("Программа в методе setDailyForecastTimeZone()");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_TIME_ZONE='%S' WHERE CHAT_ID='%S'", zone, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static Boolean dailyForecastOn(long id) {
        logger.debug("Программа в методе dailyForecastOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_ON FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("DAILY_FORECAST_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static void setDailyForecast(long id, int isOn) {
        logger.debug("Программа в методе setDailyForecast()");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_ON=%S WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    //---------------------------------------------Прогноз по городу----------------------------------------------------
    public static void addCityRequest(long id, String code, String name) {
        logger.debug("Программа в методе addCityRequest()");

        try {
            // Если в названии города есть апостроф, его нужно удалить, чтобы SQL запрос не сломался
            while (name.contains("'")) {
                int i = name.indexOf("'");
                name = name.substring(0, i) + name.substring(i + 1);
            }
            statement.execute(String.format("INSERT INTO CITY_REQUESTS VALUES(%S, %S, '%S')", id, code, name));
            logger.info("В базу данных добавлен новый запрос города");
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getPrevCityCode(long id) {
        logger.debug("Программа в методе getPrevCity()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_CODE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            int cityCode = -1;
            while (result.next()) {
                cityCode = result.getInt(1);
            }
            return cityCode;
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getPrevCityName(long id) {
        logger.debug("Программа в методе getPrevCity()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            String cityName = "";
            while (result.next()) {
                cityName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
            }
            return cityName;
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static String getCityHistory(long id) {
        logger.debug("Программа в методе getCityHistory()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));

            StringBuilder sb = new StringBuilder("История запросов по городам:\n");
            int i = 1;
            while (result.next()) {
                sb.append(i);
                sb.append(") ");
                String name = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                sb.append(name);
                sb.append("\n");
                i++;
            }
            if (i != 1) sb.append("\nВыберите номер города:");
            return sb.toString();
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static void clearCityHistory(long id) {
        logger.debug("Программа в методе clearCityHistory()");

        try {
            statement.execute(String.format("DELETE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            logger.info(String.format("Очищена история запросов по городам из чата %s", id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getCityCodeByIndex(long id, int index) {
        logger.debug("Программа в методе getCityByIndex()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_CODE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            int cityCode;
            int i = 1;
            while (result.next()) {
                cityCode = result.getInt(1);
                if (i == index) return cityCode;
                i++;
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getCityNameByIndex(long id, int index) {
        logger.debug("Программа в методе getCityByIndex()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            String cityName;
            int i = 1;
            while (result.next()) {
                cityName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                if (i == index) return cityName;
                i++;
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    //---------------------------------------------Прогноз по геолокации------------------------------------------------
    public static void addGeoRequest(long id, String code, String name) {
        logger.debug("Программа в методе addGeoRequest()");

        try {
            // Если в названии геолокации есть апостроф, его нужно удалить, чтобы SQL запрос не сломался
            while (name.contains("'")) {
                int i = name.indexOf("'");
                name = name.substring(0, i) + name.substring(i + 1);
            }
            statement.execute(String.format("INSERT INTO GEO_REQUESTS VALUES(%S, %S, '%S')", id, code, name));
            logger.info("В базу данных добавлен новый запрос геолокации");
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getPrevGeoCode(long id) {
        logger.debug("Программа в методе getPrevGeo()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_CODE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            int geoCode = -1;
            while (result.next()) {
                geoCode = result.getInt(1);
            }
            return geoCode;
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getPrevGeoName(long id) {
        logger.debug("Программа в методе getPrevGeo()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            String geoName = "";
            while (result.next()) {
                geoName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
            }
            return geoName;
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static String getGeoHistory(long id) {
        logger.debug("Программа в методе getGeoHistory()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));

            StringBuilder sb = new StringBuilder("История запросов по геолокации:\n");
            int i = 1;
            while (result.next()) {
                sb.append(i);
                sb.append(") ");
                String name = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                sb.append(name);
                sb.append("\n");
                i++;
            }
            if (i != 1) sb.append("\nВыберите номер локации:");
            return sb.toString();
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static void clearGeoHistory(long id) {
        logger.debug("Программа в методе clearGeoHistory()");

        try {
            statement.execute(String.format("DELETE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            logger.info(String.format("Очищена история запросов по геолокации из чата %s", id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getGeoCodeByIndex(long id, int index) {
        logger.debug("Программа в методе getGeoByIndex()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_CODE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            int geoCode;
            int i = 1;
            while (result.next()) {
                geoCode = result.getInt(1);
                if (i == index) return geoCode;
                i++;
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getGeoNameByIndex(long id, int index) {
        logger.debug("Программа в методе getGeoByIndex()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            String geoName;
            int i = 1;
            while (result.next()) {
                geoName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                if (i == index) return geoName;
                i++;
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    //---------------------------------------------Получить значения флагов---------------------------------------------
    public static Boolean dailyForecastTimeInputOn(long id) {
        logger.debug("Программа в методе dailyForecastTimeInputOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_TIME_INPUT_ON FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("DAILY_FORECAST_TIME_INPUT_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static Boolean dailyForecastCityInputOn(long id) {
        logger.debug("Программа в методе dailyForecastCityInputOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_CITY_INPUT_ON FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("DAILY_FORECAST_CITY_INPUT_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static Boolean cityNameInputOn(long id) {
        logger.debug("Программа в методе cityNameInputOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME_INPUT_ON FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("CITY_NAME_INPUT_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static Boolean cityNumberChooseOn(long id) {
        logger.debug("Программа в методе cityNumberChooseOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NUMBER_CHOOSE_ON FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("CITY_NUMBER_CHOOSE_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static Boolean locationInputOn(long id) {
        logger.debug("Программа в методе locationInputOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT LOCATION_INPUT_ON FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("LOCATION_INPUT_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static Boolean cityHistoryChooseOn(long id) {
        logger.debug("Программа в методе cityHistoryChooseOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_HISTORY_CHOOSE_ON FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("CITY_HISTORY_CHOOSE_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static Boolean geoHistoryChooseOn(long id) {
        logger.debug("Программа в методе geoHistoryChooseOn()");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_HISTORY_CHOOSE_ON FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("GEO_HISTORY_CHOOSE_ON");
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    //---------------------------------------------Установить значения флагов-------------------------------------------
    public static void setDailyForecastTimeInput(long id, int isOn) {
        logger.debug("Программа в методе setDailyForecastTimeInput()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET DAILY_FORECAST_TIME_INPUT_ON='%S' WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void setDailyForecastCityInput(long id, int isOn) {
        logger.debug("Программа в методе setDailyForecastCityInput()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET DAILY_FORECAST_CITY_INPUT_ON='%S' WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void setCityNameInput(long id, int isOn) {
        logger.debug("Программа в методе setCityNameInput()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET CITY_NAME_INPUT_ON='%S' WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void setCityNumberChoose(long id, int isOn) {
        logger.debug("Программа в методе setCityNumberChoose()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET CITY_NUMBER_CHOOSE_ON='%S' WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void setLocationInput(long id, int isOn) {
        logger.debug("Программа в методе setLocationInput()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET LOCATION_INPUT_ON='%S' WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void setCityHistoryChoose(long id, int isOn) {
        logger.debug("Программа в методе setCityHistoryChoose()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET CITY_HISTORY_CHOOSE_ON='%S' WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void setGeoHistoryChoose(long id, int isOn) {
        logger.debug("Программа в методе setGeoHistoryChoose()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET GEO_HISTORY_CHOOSE_ON='%S' WHERE CHAT_ID='%S'", isOn, id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void resetAllFlags(long id) {
        logger.debug("Программа в методе resetAllFlags()");

        try {
            statement.execute(String.format("UPDATE CHATS_FLAGS SET DAILY_FORECAST_TIME_INPUT_ON=0 WHERE CHAT_ID='%S'", id));
            statement.execute(String.format("UPDATE CHATS_FLAGS SET DAILY_FORECAST_CITY_INPUT_ON=0 WHERE CHAT_ID='%S'", id));
            statement.execute(String.format("UPDATE CHATS_FLAGS SET CITY_NAME_INPUT_ON=0 WHERE CHAT_ID='%S'", id));
            statement.execute(String.format("UPDATE CHATS_FLAGS SET CITY_NUMBER_CHOOSE_ON=0 WHERE CHAT_ID='%S'", id));
            statement.execute(String.format("UPDATE CHATS_FLAGS SET LOCATION_INPUT_ON=0 WHERE CHAT_ID='%S'", id));
            statement.execute(String.format("UPDATE CHATS_FLAGS SET CITY_HISTORY_CHOOSE_ON=0 WHERE CHAT_ID='%S'", id));
            statement.execute(String.format("UPDATE CHATS_FLAGS SET GEO_HISTORY_CHOOSE_ON=0 WHERE CHAT_ID='%S'", id));
        }
        catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
