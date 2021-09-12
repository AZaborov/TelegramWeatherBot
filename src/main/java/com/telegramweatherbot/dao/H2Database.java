package com.telegramweatherbot.dao;

import com.pengrad.telegrambot.TelegramBot;
import com.telegramweatherbot.model.LocationByCity;
import com.telegramweatherbot.model.TimeZone;
import com.telegramweatherbot.model.DailyForecastAlarmClock;
import com.telegramweatherbot.service.Utils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.commons.text.WordUtils;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class H2Database {

    private static final Logger logger = Logger.getLogger(H2Database.class);
    public static H2Database database = new H2Database();
    private Connection connection;
    private static Statement statement;

    public H2Database() {
        logger.debug("Программа в конструкторе класса H2Database");

        connect();
        createTables();
    }

    private void connect() {
        logger.debug("Программа в методе connect() класса H2Database");

        try {
            Class.forName("org.h2.Driver");

            String url = Utils.getProperties().getProperty("databaseUrl");
            String user = Utils.getProperties().getProperty("databaseUser");
            String password = Utils.getProperties().getProperty("databasePassword");

            JdbcConnectionPool cp = JdbcConnectionPool.create(url, user, password);
            connection = cp.getConnection();
            //connection = DriverManager.getConnection(url, user, password);

            logger.info("Подключена база данных");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void createTables() {
        logger.debug("Программа в методе createTables() класса H2Database");

        try {
            statement = connection.createStatement();

            ScriptRunner sr = new ScriptRunner(connection);
            Reader reader = new BufferedReader(new FileReader("src/main/scripts/createTables.sql"));
            sr.runScript(reader);

            logger.info("В базе данных созданы таблицы");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void addChat(long id) {
        logger.debug("Программа в методе addChat() класса H2Database");

        try {
            statement.execute(String.format("INSERT INTO DAILY_FORECAST_SETTINGS VALUES (%S, '09:00:00', 'Moscow, Russia, Moscow', 294021, 'Europe/Moscow', 0)", id));
            logger.info(String.format("В базу данных добавлен новый чат (id %s)", id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Boolean chatExists(long id) {
        logger.debug("Программа в методе chatExists() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT * FROM CHATS_FLAGS WHERE CHAT_ID='%S'", id));
            return result.next();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static void addCities(long id, LocationByCity[] cities) {
        logger.debug("Программа в методе addCities() класса H2Database");

        try {
            statement.execute(String.format("DELETE FROM CITIES WHERE CHAT_ID='%S'", id));

            for (LocationByCity city : cities) {

                StringBuilder name;
                name = new StringBuilder();
                name.append(city.getLocalizedName());
                name.append(", ");
                name.append(city.getCountry().getLocalizedName());
                name.append(", ");
                name.append(city.getAdministrativeArea().getLocalizedName());

                // Если в названии города есть апостроф, его нужно удалить, чтобы SQL запрос не сломался
                String cityName = name.toString();
                cityName = cityName.replace("'", " ");

                String timeZone = city.getTimeZone().getName();
                statement.execute(String.format("INSERT INTO CITIES VALUES (%S, %S, '%S', '%S')", id, city.getKey(), cityName, timeZone));
            }

            logger.info(String.format("В базу данных добавлены результаты запроса городов для чата %s", id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static ArrayList<LocationByCity> getCities(long id) {
        logger.debug("Программа в методе getCities() класса H2Database");

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
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    //---------------------------------------------Ежедневный прогноз---------------------------------------------------
    public static Time getDailyForecastTime(long id) {
        logger.debug("Программа в методе getDailyForecastTime() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_TIME FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getTime(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        //TODO плохая практика хардкодить время по умолчанию
        return Time.valueOf("09:00:00");
    }

    public static void setDailyForecastTime(long id, Time time) {
        logger.debug("Программа в методе setDailyForecastTime() класса H2Database");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_TIME='%S' WHERE CHAT_ID='%S'", time, id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getDailyForecastCityName(long id) {
        logger.debug("Программа в методе getDailyForecastCityName() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_CITY_NAME FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return WordUtils.capitalizeFully(result.getString(1).toLowerCase());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        //TODO аналогичено предыдущему замечанию
        return "Moscow, Russia, Moscow";
    }

    public static void setDailyForecastCityName(long id, String name) {
        logger.debug("Программа в методе setDailyForecastCityName() класса H2Database");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_CITY_NAME='%S' WHERE CHAT_ID='%S'", name, id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getDailyForecastCityCode(long id) {
        logger.debug("Программа в методе getDailyForecastCityCode() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_CITY_CODE FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getString("DAILY_FORECAST_CITY_CODE");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        //TODO
        return "294021";
    }

    public static void setDailyForecastCityCode(long id, String code) {
        logger.debug("Программа в методе setDailyForecastCityCode() класса H2Database");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_CITY_CODE=%S WHERE CHAT_ID='%S'", code, id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static String getDailyForecastTimeZone(long id) {
        logger.debug("Программа в методе getDailyForecastTimeZone() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_TIME_ZONE FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                String timeZone = result.getString("DAILY_FORECAST_TIME_ZONE");
                return Utils.formatTimeZone(timeZone);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        //TODO
        return "Europe/Moscow";
    }

    public static void setDailyForecastTimeZone(long id, String zone) {
        logger.debug("Программа в методе setDailyForecastTimeZone() класса H2Database");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_TIME_ZONE='%S' WHERE CHAT_ID='%S'", zone, id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static Boolean dailyForecastOn(long id) {
        logger.debug("Программа в методе dailyForecastOn() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT DAILY_FORECAST_ON FROM DAILY_FORECAST_SETTINGS WHERE CHAT_ID='%S'", id));
            if (result.next()) {
                return result.getBoolean("DAILY_FORECAST_ON");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static void setDailyForecast(long id, int isOn) {
        logger.debug("Программа в методе setDailyForecast() класса H2Database");

        try {
            statement.execute(String.format("UPDATE DAILY_FORECAST_SETTINGS SET DAILY_FORECAST_ON=%S WHERE CHAT_ID='%S'", isOn, id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    // В случае если программа была перезапущена, восстанавливаем таймеры для ежедневного прогноза
    public void setAlarmClocks(HashMap<Long, DailyForecastAlarmClock> dailyForecastAlarmClocks, TelegramBot bot) {
        logger.debug("Программа в методе setAlarmClocks() класса H2Database");

        try {
            ResultSet result = statement.executeQuery("SELECT * FROM DAILY_FORECAST_SETTINGS");

            while (result.next()) {
                long chatId = result.getLong("CHAT_ID");
                Time alarmTime = result.getTime("DAILY_FORECAST_TIME");
                String cityName = result.getString("DAILY_FORECAST_CITY_NAME");
                int cityCode = result.getInt("DAILY_FORECAST_CITY_CODE");
                String dailyForecastTimeZone = result.getString("DAILY_FORECAST_TIME_ZONE");
                boolean forecastOn = result.getBoolean("DAILY_FORECAST_ON");

                DailyForecastAlarmClock clock = new DailyForecastAlarmClock(chatId, alarmTime, cityName, String.valueOf(cityCode), dailyForecastTimeZone);
                dailyForecastAlarmClocks.put(chatId, clock);
                if (forecastOn) clock.startClock();
                logger.info("ДАННЫЕ ИЗ БАЗЫ СОХРАНИЛИСЬ");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    //---------------------------------------------Прогноз по городу----------------------------------------------------
    public static void addCityRequest(long id, String code, String name) {
        logger.debug("Программа в методе addCityRequest() класса H2Database");

        try {
            // Если в названии города есть апостроф, его нужно удалить, чтобы SQL запрос не сломался
            name = name.replace("'", " ");
            statement.execute(String.format("INSERT INTO CITY_REQUESTS VALUES(%S, %S, '%S')", id, code, name));
            logger.info("В базу данных добавлен новый запрос города");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getPrevCityCode(long id) {
        logger.debug("Программа в методе getPrevCity() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_CODE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            int cityCode = -1;
            while (result.next()) {
                cityCode = result.getInt(1);
            }
            return cityCode;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getPrevCityName(long id) {
        logger.debug("Программа в методе getPrevCity() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            String cityName = "";
            while (result.next()) {
                cityName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
            }
            return cityName;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static ArrayList<String> getCityHistory(long id) {
        logger.debug("Программа в методе getCityHistory() класса H2Database");
        ArrayList<String> history = new ArrayList<>();

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));

            while (result.next()) {
                history.add(result.getString(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return history;
    }

    public static void clearCityHistory(long id) {
        logger.debug("Программа в методе clearCityHistory() класса H2Database");

        try {
            statement.execute(String.format("DELETE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            logger.info(String.format("Очищена история запросов по городам из чата %s", id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getCityCodeByIndex(long id, int index) {
        logger.debug("Программа в методе getCityByIndex() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_CODE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            int cityCode;
            int i = 1;
            while (result.next()) {
                cityCode = result.getInt(1);
                if (i == index) return cityCode;
                i++;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getCityNameByIndex(long id, int index) {
        logger.debug("Программа в методе getCityByIndex() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
            String cityName;
            int i = 1;
            while (result.next()) {
                cityName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                if (i == index) return cityName;
                i++;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    //---------------------------------------------Прогноз по геолокации------------------------------------------------
    public static void addGeoRequest(long id, String code, String name) {
        logger.debug("Программа в методе addGeoRequest() класса H2Database");

        try {
            // Если в названии геолокации есть апостроф, его нужно удалить, чтобы SQL запрос не сломался
            name = name.replace("'", " ");
            statement.execute(String.format("INSERT INTO GEO_REQUESTS VALUES(%S, %S, '%S')", id, code, name));
            logger.info("В базу данных добавлен новый запрос геолокации");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getPrevGeoCode(long id) {
        logger.debug("Программа в методе getPrevGeo() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_CODE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            int geoCode = -1;
            while (result.next()) {
                geoCode = result.getInt(1);
            }
            return geoCode;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getPrevGeoName(long id) {
        logger.debug("Программа в методе getPrevGeo() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            String geoName = "";
            while (result.next()) {
                geoName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
            }
            return geoName;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public static ArrayList<String> getGeoHistory(long id) {
        logger.debug("Программа в методе getGeoHistory() класса H2Database");

        try (ResultSet result = statement.executeQuery(String.format("SELECT GEO_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));) {
            ArrayList<String> history = new ArrayList<>();

            while (result.next()) {
                history.add(result.getString(1));
            }
            return history;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static void clearGeoHistory(long id) {
        logger.debug("Программа в методе clearGeoHistory() класса H2Database");

        try {
            statement.execute(String.format("DELETE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            logger.info(String.format("Очищена история запросов по геолокации из чата %s", id));
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static int getGeoCodeByIndex(long id, int index) {
        logger.debug("Программа в методе getGeoByIndex() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_CODE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            int geoCode;
            int i = 1;
            while (result.next()) {
                geoCode = result.getInt(1);
                if (i == index) return geoCode;
                i++;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return -1;
    }

    public static String getGeoNameByIndex(long id, int index) {
        logger.debug("Программа в методе getGeoByIndex() класса H2Database");

        try {
            ResultSet result = statement.executeQuery(String.format("SELECT GEO_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
            String geoName;
            int i = 1;
            while (result.next()) {
                geoName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                if (i == index) return geoName;
                i++;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }
}
