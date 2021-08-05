package com.telegramweatherbot.service;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.model.HourForecast;
import com.telegramweatherbot.model.LocationByCity;
import com.telegramweatherbot.model.LocationByGeo;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class TelegramWeatherBot {

    private static final Logger logger = Logger.getLogger(TelegramWeatherBot.class);
    private static final Gson gson = new Gson();
    private static final HashMap<Long, DailyForecastAlarmClock> dailyForecastAlarmClocks = new HashMap<>();

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        logger.debug("Программа запущена");

        String telegramBotToken = "1916891296:AAGTy3CCc2veSmK1wwpotT84amHi-JLxCFk";
        TelegramBot bot = new TelegramBot(telegramBotToken);
        try {
            H2Database.createTables();
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }

        //---------------------------------------------Обработка обновлений---------------------------------------------
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                long chatId = update.message().from().id();
                String userName = update.message().from().username();
                String userMessage = update.message().text();
                logger.info(String.format("От пользователя %s в чате %s пришло сообщение", userName, chatId));

                if (!H2Database.chatExists(chatId)) {
                    H2Database.addChat(chatId);
                }

                if (userMessage != null) {
                    switch (userMessage) {
                        case "/start": {
                            logger.debug("Программа получила /start");
                            H2Database.resetAllFlags(chatId);

                            String message = "Бот для прогноза погоды. Введите /help для получения списка доступных команд.";
                            bot.execute(new SendMessage(chatId, message));
                            break;
                        }
                        case "/help": {
                            logger.debug("Программа получила /help");
                            H2Database.resetAllFlags(chatId);

                            String message =
                                    "/forecastcity - получить прогноз по выбранному городу\n" +
                                    "/forecastgeo - получить прогноз по выбранной геолокации\n" +
                                    "/prevforecastcity - получить прогноз по предыдущему городу\n" +
                                    "/prevforecastgeo - получить прогноз по предыдущей геолокации\n" +
                                    "/forecastcityhistory - посмотреть историю запросов по городу и выбрать один из них\n" +
                                    "/forecastgeohistory - посмотреть историю запросов по геолокации и выбрать один из них\n" +
                                    "/forecastcityclear - очистить историю запросов по городу\n" +
                                    "/forecastgeoclear - очистить историю запросов по геолокации\n" +
                                    "/toggledaily - включить/выключить ежедневный прогноз (по умолчанию выключен)\n" +
                                    "/setdailytime - установить время ежедневного прогноза (по умолчанию 9:00)\n" +
                                    "/setdailycity - установить город ежедневного прогноза (по умолчанию Москва)\n";

                            bot.execute(new SendMessage(chatId, message));
                            break;
                        }
                        case "/forecastcity": {
                            logger.debug("Программа получила /forecastcity");
                            H2Database.resetAllFlags(chatId);
                            bot.execute(new SendMessage(chatId, "Введите название города"));
                            H2Database.setCityNameInput(chatId, 1);
                            break;
                        }
                        case "/forecastgeo": {
                            logger.debug("Программа получила /forecastgeo");
                            H2Database.resetAllFlags(chatId);
                            bot.execute(new SendMessage(chatId, "Отправьте геолокацию"));
                            H2Database.setLocationInput(chatId, 1);
                            break;
                        }
                        case "/prevforecastcity": {
                            logger.debug("Программа получила /prevforecastcity");
                            H2Database.resetAllFlags(chatId);
                            int cityCode = H2Database.getPrevCityCode(chatId);
                            String cityName = H2Database.getPrevCityName(chatId);

                            if (cityCode == -1 ||cityName.equals("")) {
                                bot.execute(new SendMessage(chatId, "Не найдено предыдущего запроса"));
                                logger.error(String.format("В базе данных запросов по городам для чата %s нет записей", chatId));
                            }
                            else {
                                String message = getForecastMessage(String.valueOf(cityCode), "Город: " + cityName);
                                bot.execute(new SendMessage(chatId, message));
                                logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chatId));
                            }
                            break;
                        }
                        case "/prevforecastgeo": {
                            logger.debug("Программа получила /prevforecastgeo");
                            H2Database.resetAllFlags(chatId);
                            int locationCode = H2Database.getPrevGeoCode(chatId);
                            String locationName = H2Database.getPrevGeoName(chatId);

                            if (locationCode == -1 || locationName.equals("")) {
                                bot.execute(new SendMessage(chatId, "Не найдено предыдущего запроса"));
                                logger.error(String.format("В базе данных запросов по геолокации для чата %s нет записей", chatId));
                            }
                            else {
                                String message = getForecastMessage(String.valueOf(locationCode), "Локация: " + locationName);
                                bot.execute(new SendMessage(chatId, message));
                                logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chatId));
                            }
                            break;
                        }
                        case "/forecastcityhistory": {
                            logger.debug("Программа получила /forecastcityhistory");
                            H2Database.resetAllFlags(chatId);
                            String history = H2Database.getCityHistory(chatId);
                            bot.execute(new SendMessage(chatId, history));
                            logger.info(String.format("Программа успешно отправила историю запросов по городам в чат %s", chatId));
                            H2Database.setCityHistoryChoose(chatId, 1);
                            break;
                        }
                        case "/forecastgeohistory": {
                            logger.debug("Программа получила /forecastgeohistory");
                            H2Database.resetAllFlags(chatId);
                            String history = H2Database.getGeoHistory(chatId);
                            bot.execute(new SendMessage(chatId, history));
                            logger.info(String.format("Программа успешно отправила историю запросов по геолокаци в чат %s", chatId));
                            H2Database.setGeoHistoryChoose(chatId, 1);
                            break;
                        }
                        case "/forecastcityclear": {
                            logger.debug("Программа получила /forecastcityclear");
                            H2Database.resetAllFlags(chatId);
                            H2Database.clearCityHistory(chatId);
                            bot.execute(new SendMessage(chatId, "История запросов по городам очищена"));
                            logger.info(String.format("Программа успешно очистила историю запросов по городам для чата %s", chatId));
                            break;
                        }
                        case "/forecastgeoclear": {
                            logger.debug("Программа получила /forecastgeoclear");
                            H2Database.resetAllFlags(chatId);
                            H2Database.clearGeoHistory(chatId);
                            bot.execute(new SendMessage(chatId, "История запросов по геолокации очищена"));
                            logger.info(String.format("Программа успешно очистила историю запросов по геолокации для чата %s", chatId));
                            break;
                        }
                        case "/toggledaily": {
                            logger.debug("Программа получила /toggledaily");
                            H2Database.resetAllFlags(chatId);

                            // Прогноз включён, выключаем
                            if (H2Database.dailyForecastOn(chatId)) {
                                dailyForecastAlarmClocks.get(chatId).stopClock();
                                dailyForecastAlarmClocks.remove(chatId);
                                bot.execute(new SendMessage(chatId, "Ежедневый прогноз выключен"));
                                logger.info(String.format("Пользователь %s в чате %s выключил ежедневый прогноз", userName, chatId));
                                H2Database.setDailyForecast(chatId, 0);
                            }
                            // Прогноз выключен, включаем
                            else {
                                Time time = H2Database.getDailyForecastTime(chatId);
                                String name = H2Database.getDailyForecastCityName(chatId);
                                String code = H2Database.getDailyForecastCityCode(chatId);
                                String zone = H2Database.getDailyForecastTimeZone(chatId);

                                DailyForecastAlarmClock clock = new DailyForecastAlarmClock(chatId, bot, time, name, code, zone);
                                clock.startClock();
                                dailyForecastAlarmClocks.put(chatId, clock);

                                StringBuilder message = new StringBuilder();
                                message.append("Ежедневый прогноз включён\n");
                                message.append("Время: ");
                                message.append(time);
                                message.append("\nГород: ");
                                message.append(name);

                                bot.execute(new SendMessage(chatId, message.toString()));
                                logger.info(String.format("Пользователь %s в чате %s включил ежедневый прогноз", userName, chatId));
                                H2Database.setDailyForecast(chatId, 1);
                            }

                            break;
                        }
                        case "/setdailytime": {
                            logger.debug("Программа получила /setdailytime");
                            H2Database.resetAllFlags(chatId);
                            bot.execute(new SendMessage(chatId, "Введите время для ежедневного прогноза (в формате hh:mm:ss):"));
                            H2Database.setDailyForecastTimeInput(chatId, 1);
                            break;
                        }
                        case "/setdailycity": {
                            logger.debug("Программа получила /setdailycity");
                            H2Database.resetAllFlags(chatId);
                            bot.execute(new SendMessage(chatId, "Введите город для ежедневного прогноза:"));
                            H2Database.setDailyForecastCityInput(chatId, 1);
                            break;
                        }
                        default: {
                            // Выбор города среди вариантов с одинаковыми названиями (для прогноза по городу и ежедневного прогноза)
                            if (H2Database.cityNumberChooseOn(chatId)) {
                                logger.info(String.format("Программа получила от пользователя %s в чате %s номер города", userName, chatId));
                                String input = update.message().text();

                                try {
                                    logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
                                    int cityNum = Integer.parseInt(input);
                                    ArrayList<LocationByCity> cities = H2Database.getCities(chatId);
                                    String cityName = cities.get(cityNum - 1).getLocalizedName();
                                    String cityCode = cities.get(cityNum - 1).getKey();
                                    String timeZone = cities.get(cityNum - 1).getTimeZone().getName();

                                    // Выбор города для ежедневного прогноза
                                    if (H2Database.dailyForecastCityInputOn(chatId)) {
                                        H2Database.setDailyForecastCityName(chatId, cityName);
                                        H2Database.setDailyForecastCityCode(chatId, cityCode);
                                        H2Database.setDailyForecastTimeZone(chatId, timeZone);

                                        if (dailyForecastAlarmClocks.containsKey(chatId)) {
                                            dailyForecastAlarmClocks.get(chatId).setCityName(cityName);
                                            dailyForecastAlarmClocks.get(chatId).setCityCode(cityCode);
                                            dailyForecastAlarmClocks.get(chatId).setTimeZone(timeZone);
                                            dailyForecastAlarmClocks.get(chatId).startClock();
                                        }
                                        bot.execute(new SendMessage(chatId, String.format("Город %s установлен для ежедневного прогноза", cityName)));
                                        logger.info(String.format("Пользователь %s в чате %s установил город %s ежедневного прогноза", userName, chatId, cityName));

                                        H2Database.setDailyForecastCityInput(chatId, 0);
                                    }
                                    // Выбор города для текущего прогноза на 12 часов
                                    else {
                                        bot.execute(new SendMessage(chatId, getForecastMessage(cityCode, "Город: " + cityName)));
                                        H2Database.addCityRequest(chatId, cityCode, cityName);
                                        logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chatId));
                                    }
                                }
                                catch (NullPointerException e) {
                                    logger.error(e.getMessage(), e);
                                }
                                catch (NumberFormatException e) {
                                    bot.execute(new SendMessage(chatId, "Введено не число"));
                                    logger.error(e.getMessage(), e);
                                }
                                catch (ArrayIndexOutOfBoundsException e) {
                                    bot.execute(new SendMessage(chatId, "Города с таким номером нет в списке"));
                                    logger.error(e.getMessage(), e);
                                }

                                H2Database.setCityNumberChoose(chatId, 0);
                            }

                            // Запросить список городов
                            // Нужно для получения прогноза на 12 часов, либо для ежедневного прогноза
                            else if (H2Database.cityNameInputOn(chatId) || H2Database.dailyForecastCityInputOn(chatId)) {
                                String input = update.message().text();
                                logger.info(String.format("Программа получила от пользователя %s в чате %s название города", userName, chatId));

                                String contents = HTTPRequest.getCities(input);
                                LocationByCity[] cities = gson.fromJson(contents, LocationByCity[].class);
                                logger.debug(String.format("Программа получила от пользователя %s в чате %s json сообщение с найденными городами и распрасила его", userName, chatId));

                                StringBuilder message = new StringBuilder();
                                if (cities.length == 0) {
                                    message.append("Не найдено городов с таким названием");
                                    logger.error(String.format("Пользователь %s в чате %s ввёл название несуществующего города", userName, chatId));
                                }
                                else {
                                    message.append("Выберите номер города:\n");
                                    for (int i = 0; i < cities.length; i++) {
                                        message.append(i + 1);
                                        message.append(") ");
                                        message.append(cities[i].getLocalizedName());
                                        message.append(", ");
                                        message.append(cities[i].getCountry().getLocalizedName());
                                        message.append(", ");
                                        message.append(cities[i].getAdministrativeArea().getLocalizedName());
                                        message.append("\n");
                                        H2Database.setCityNumberChoose(chatId,1);
                                    }
                                    logger.debug(String.format("Программа составила список городов, основываясь на введённых пользователем %s в чате %s данных", userName, chatId));
                                }

                                bot.execute(new SendMessage(chatId, message.toString()));
                                H2Database.addCities(chatId, cities);
                                H2Database.setCityNameInput(chatId, 0);
                            }

                            // Выбор города среди истории запросов
                            else if (H2Database.cityHistoryChooseOn(chatId)) {
                                String input = update.message().text();

                                try {
                                    logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
                                    int index = Integer.parseInt(input);
                                    int cityCode = H2Database.getCityCodeByIndex(chatId, index);
                                    String cityName = H2Database.getCityNameByIndex(chatId, index);

                                    if (cityCode == -1 || cityName.equals("")) {
                                        bot.execute(new SendMessage(chatId, "Города с таким номером нет в списке"));
                                        logger.error(String.format("Города с таким номером нет в базе данных для чата %s", chatId));
                                    }
                                    else {
                                        String message = getForecastMessage(String.valueOf(cityCode), "Город: " + cityName);
                                        bot.execute(new SendMessage(chatId, message));
                                        logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chatId));
                                    }
                                }
                                catch (NumberFormatException e) {
                                    bot.execute(new SendMessage(chatId, "Введено не число"));
                                    logger.error(e.getMessage(), e);
                                }

                                H2Database.setCityHistoryChoose(chatId, 0);
                            }

                            // Выбор геолокации среди истории запросов
                            else if (H2Database.geoHistoryChooseOn(chatId)) {
                                String input = update.message().text();

                                try {
                                    logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
                                    int index = Integer.parseInt(input);
                                    int locationCode = H2Database.getGeoCodeByIndex(chatId, index);
                                    String locationName = H2Database.getGeoNameByIndex(chatId, index);

                                    if (locationCode == -1 || locationName.equals("")) {
                                        bot.execute(new SendMessage(chatId, "Локации с таким номером нет в списке"));
                                        logger.error(String.format("Локации с таким номером нет в базе данных для чата %s", chatId));
                                    }
                                    else {
                                        String message = getForecastMessage(String.valueOf(locationCode), "Локация " + locationName);
                                        bot.execute(new SendMessage(chatId, message));
                                        logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chatId));
                                    }
                                }
                                catch (NumberFormatException e) {
                                    bot.execute(new SendMessage(chatId, "Введено не число"));
                                    logger.error(e.getMessage(), e);
                                }
                                H2Database.setGeoHistoryChoose(chatId, 0);
                            }

                            // Установка времени для ежедневного прогноза
                            else if (H2Database.dailyForecastTimeInputOn(chatId)) {
                                String input = update.message().text();

                                try {
                                    Time time = Time.valueOf(input);
                                    H2Database.setDailyForecastTime(chatId, time);
                                    if (dailyForecastAlarmClocks.containsKey(chatId)) {
                                        dailyForecastAlarmClocks.get(chatId).setAlarmTime(time);
                                        dailyForecastAlarmClocks.get(chatId).startClock();
                                    }
                                    bot.execute(new SendMessage(chatId, String.format("Время ежедневного прогноза установлено на %s", time)));
                                    logger.info(String.format("Пользователь %s установил время ежедневного прогноза на %s", userName, time));
                                }
                                catch(Exception e) {
                                    bot.execute(new SendMessage(chatId, "Время введено неверно"));
                                    logger.error(e.getMessage(), e);
                                }

                                H2Database.setDailyForecastTimeInput(chatId, 0);
                            }
                        }
                    }
                }

                // Прогноз погоды на 12 часов по геолокации
                else if (H2Database.locationInputOn(chatId)) {
                    logger.debug("Присланное пользователем сообщение не является текстом");
                    try {
                        logger.warn("Присланное пользователем сообщение может быть не геолокацией");
                        Float latitude = update.message().location().latitude();
                        Float longitude = update.message().location().longitude();

                        String contents = HTTPRequest.getLocation(latitude, longitude);
                        Gson gson = new Gson();
                        LocationByGeo location = gson.fromJson(contents, LocationByGeo.class);
                        logger.debug("Программа получила json сообщение с найденной локацией и распрасила его");

                        String locationName = location.getLocalizedName() + ", " + location.getAdministrativeArea().getLocalizedName();
                        String message = getForecastMessage(location.getKey(), "Локация: " + locationName);

                        bot.execute(new SendMessage(chatId, message));
                        H2Database.addGeoRequest(chatId, location.getKey(), locationName);
                        logger.info("Программа успешно отправила прогноз погоды пользователю");
                    }
                    catch (Exception e) {
                        bot.execute(new SendMessage(chatId, "Неверный ввод геолокации"));
                        logger.error(e.getMessage(), e);
                    }

                    H2Database.setLocationInput(chatId, 0);
                }
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public static String getForecastMessage(String code, String prevMessage) {
        logger.debug("Программа в методе getForecastMessage()");

        String contents = HTTPRequest.get12HourForecast(code);
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
