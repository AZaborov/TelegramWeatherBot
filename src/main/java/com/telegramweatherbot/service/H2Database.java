package com.telegramweatherbot.service;

import org.apache.log4j.Logger;

import java.sql.*;

public class H2Database {

    private static final Logger logger = Logger.getLogger(H2Database.class);
    private static final String url      = "jdbc:h2:~/test";
    private static final String user     = "sa";
    private static final String password = "";

    private H2Database(){}

    public static void createTables() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS CHATS");
                statement.execute("DROP TABLE IF EXISTS CITY_REQUESTS");
                statement.execute("DROP TABLE IF EXISTS GEO_REQUESTS");

                statement.execute("CREATE TABLE CHATS(CHAT_ID BIGINT PRIMARY KEY, DAILY_FORECAST_TIME TIME, DAILY_FORECAST_ON BIT)");
                statement.execute("CREATE TABLE CITY_REQUESTS(CHAT_ID BIGINT, LOCATION_CODE INT)");
                statement.execute("CREATE TABLE GEO_REQUESTS(CHAT_ID BIGINT, LOCATION_CODE INT)");

                logger.info("В базе данных созданы таблицы");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addChat(long id) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("INSERT INTO CHATS VALUES (%S, '09:00:00', 0)", id));
                logger.info("В базу данных добавлен новый пользователь");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addCityRequest(long id, String code) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("INSERT INTO CITY_REQUESTS VALUES(%S, %S)", id, code));
                logger.info("В базу данных добавлен новый запрос города");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addGeoRequest(long id, String code) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("INSERT INTO GEO_REQUESTS VALUES(%S, %S)", id, code));
                logger.info("В базу данных добавлен новый запрос геолокации");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getPrevCity(long id) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT LOCATION_CODE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
                int cityCode = -1;
                while (result.next()) {
                    cityCode = result.getInt(1);
                }
                return cityCode;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static int getPrevGeo(long id) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT LOCATION_CODE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
                int cityCode = -1;
                while (result.next()) {
                    cityCode = result.getInt(1);
                }
                return cityCode;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Boolean chatExists(long id) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT * FROM CHATS WHERE CHAT_ID='%S'", id));
                return result.next();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
