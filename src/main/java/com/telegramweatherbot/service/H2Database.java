package com.telegramweatherbot.service;

import org.apache.commons.text.WordUtils;
import org.apache.log4j.Logger;
import java.sql.*;

public class H2Database {

    private static final Logger logger = Logger.getLogger(H2Database.class);
    private static final String url      = "jdbc:h2:~/test";
    private static final String user     = "sa";
    private static final String password = "";

    private H2Database(){}

    public static void createTables() throws ClassNotFoundException {
        logger.debug("Программа в методе createTables()");

        Class.forName("org.h2.Driver");
        logger.info("Подключена база данных");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS CHATS");
                statement.execute("DROP TABLE IF EXISTS CITY_REQUESTS");
                statement.execute("DROP TABLE IF EXISTS GEO_REQUESTS");

                statement.execute("CREATE TABLE CHATS(CHAT_ID BIGINT PRIMARY KEY, DAILY_FORECAST_TIME TIME, DAILY_FORECAST_ON BIT)");
                statement.execute("CREATE TABLE CITY_REQUESTS(CHAT_ID BIGINT, CITY_CODE INT, CITY_NAME VARCHAR(75))");
                statement.execute("CREATE TABLE GEO_REQUESTS(CHAT_ID BIGINT, LOCATION_CODE INT, LOCATION_NAME VARCHAR(75))");

                logger.info("В базе данных созданы таблицы");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
    }

    public static void addChat(long id) {
        logger.debug("Программа в методе addChat()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("INSERT INTO CHATS VALUES (%S, '09:00:00', 0)", id));
                logger.info("В базу данных добавлен новый пользователь");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
    }

    public static void addCityRequest(long id, String code, String name) {
        logger.debug("Программа в методе addCityRequest()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                while (name.contains("'")) {
                    int i = name.indexOf("'");
                    name = name.substring(0, i) + name.substring(i + 1);
                }
                statement.execute(String.format("INSERT INTO CITY_REQUESTS VALUES(%S, %S, '%S')", id, code, name));
                logger.info("В базу данных добавлен новый запрос города");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
    }

    public static void addGeoRequest(long id, String code, String name) {
        logger.debug("Программа в методе addGeoRequest()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("INSERT INTO GEO_REQUESTS VALUES(%S, %S, '%S')", id, code, name));
                logger.info("В базу данных добавлен новый запрос геолокации");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
    }

    public static int getPrevCityCode(long id) {
        logger.debug("Программа в методе getPrevCity()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT CITY_CODE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
                int cityCode = -1;
                while (result.next()) {
                    cityCode = result.getInt(1);
                }
                return cityCode;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return -1;
    }

    public static String getPrevCityName(long id) {
        logger.debug("Программа в методе getPrevCity()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
                String cityName = "";
                while (result.next()) {
                    cityName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                }
                return cityName;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return "";
    }

    public static int getPrevGeoCode(long id) {
        logger.debug("Программа в методе getPrevGeo()");

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
            logger.error("Ошибка запроса в базе данных");
        }
        return -1;
    }

    public static String getPrevGeoName(long id) {
        logger.debug("Программа в методе getPrevGeo()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT LOCATION_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
                String locationName = "";
                while (result.next()) {
                    locationName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                }
                return locationName;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return "";
    }

    public static String getCityHistory(long id) {
        logger.debug("Программа в методе getCityHistory()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
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
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return "";
    }

    public static String getGeoHistory(long id) {
        logger.debug("Программа в методе getGeoHistory()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT LOCATION_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));

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
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return "";
    }

    public static void clearCityHistory(long id) {
        logger.debug("Программа в методе clearCityHistory()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("DELETE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
                logger.info(String.format("Очищена история запросов по городам из чата %s", id));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
    }

    public static void clearGeoHistory(long id) {
        logger.debug("Программа в методе clearGeoHistory()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format("DELETE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
                logger.info(String.format("Очищена история запросов по геолокации из чата %s", id));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getCityCodeByIndex(long id, int index) {
        logger.debug("Программа в методе getCityByIndex()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT CITY_CODE FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
                int cityCode;
                int i = 1;
                while (result.next()) {
                    cityCode = result.getInt(1);
                    if (i == index) return cityCode;
                    i++;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return -1;
    }

    public static String getCityNameByIndex(long id, int index) {
        logger.debug("Программа в методе getCityByIndex()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT CITY_NAME FROM CITY_REQUESTS WHERE CHAT_ID='%S'", id));
                String cityName;
                int i = 1;
                while (result.next()) {
                    cityName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                    if (i == index) return cityName;
                    i++;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return "";
    }

    public static int getGeoCodeByIndex(long id, int index) {
        logger.debug("Программа в методе getGeoByIndex()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT LOCATION_CODE FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
                int locationCode;
                int i = 1;
                while (result.next()) {
                    locationCode = result.getInt(1);
                    if (i == index) return locationCode;
                    i++;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return -1;
    }

    public static String getGeoNameByIndex(long id, int index) {
        logger.debug("Программа в методе getGeoByIndex()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT LOCATION_NAME FROM GEO_REQUESTS WHERE CHAT_ID='%S'", id));
                String locationName;
                int i = 1;
                while (result.next()) {
                    locationName = WordUtils.capitalizeFully(result.getString(1).toLowerCase());
                    if (i == index) return locationName;
                    i++;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return "";
    }

    public static Boolean chatExists(long id) {
        logger.debug("Программа в методе chatExists()");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery(String.format("SELECT * FROM CHATS WHERE CHAT_ID='%S'", id));
                return result.next();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            logger.error("Ошибка запроса в базе данных");
        }
        return false;
    }
}
