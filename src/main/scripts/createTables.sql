CREATE TABLE IF NOT EXISTS CITY_REQUESTS(CHAT_ID BIGINT, CITY_CODE INT, CITY_NAME VARCHAR(100));
CREATE TABLE IF NOT EXISTS GEO_REQUESTS(CHAT_ID BIGINT, GEO_CODE INT, GEO_NAME VARCHAR(100));
CREATE TABLE IF NOT EXISTS CITIES(CHAT_ID BIGINT, CITY_CODE INT, CITY_NAME VARCHAR(100), CITY_TIME_ZONE VARCHAR(100));
CREATE TABLE IF NOT EXISTS DAILY_FORECAST_SETTINGS(
    CHAT_ID BIGINT PRIMARY KEY,
    DAILY_FORECAST_TIME TIME,
    DAILY_FORECAST_CITY_NAME VARCHAR(100),
    DAILY_FORECAST_CITY_CODE INT,
    DAILY_FORECAST_TIME_ZONE VARCHAR(100),
    DAILY_FORECAST_ON BIT
);
