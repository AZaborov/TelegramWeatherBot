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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TelegramWeatherBot {

    private static final Logger logger = Logger.getLogger(TelegramWeatherBot.class);
    private static Boolean cityNameInputOn = false;
    private static Boolean cityNumberChooseOn = false;
    private static Boolean locationInputOn = false;
    private static Boolean cityHistoryChooseOn = false;
    private static Boolean geoHistoryChooseOn = false;
    private static LocationByCity[] locations;

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        logger.debug("Программа запущена");

        String telegramBotToken = "1916891296:AAGTy3CCc2veSmK1wwpotT84amHi-JLxCFk";
        TelegramBot bot = new TelegramBot(telegramBotToken);
        try {
            H2Database.createTables();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Обработка обновлений
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                long userId = update.message().from().id();
                String userName = update.message().from().username();
                String userMessage = update.message().text();
                logger.info(String.format("От пользователя %s пришло сообщение", userName));

                if (!H2Database.chatExists(userId)) {
                    H2Database.addChat(userId);
                }

                if (userMessage != null) {
                    switch (userMessage) {
                        case "/start": {
                            logger.debug("Программа получила /start");
                            String message = "Бот для прогноза погоды. Введите /help для получения списка доступных команд.";
                            bot.execute(new SendMessage(userId, message));
                            break;
                        }
                        case "/help": {
                            logger.debug("Программа получила /help");
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
                                    "/settimezone - установить часовой пояс (по умолчанию МСК)\n";


                            bot.execute(new SendMessage(userId, message));
                            break;
                        }
                        case "/forecastcity": {
                            logger.debug("Программа получила /forecastcity");
                            bot.execute(new SendMessage(userId, "Введите название города"));
                            cityNameInputOn = true;
                            break;
                        }
                        case "/forecastgeo": {
                            logger.debug("Программа получила /forecastgeo");
                            bot.execute(new SendMessage(userId, "Отправьте геолокацию"));
                            locationInputOn = true;
                            break;
                        }
                        case "/prevforecastcity": {
                            logger.debug("Программа получила /prevforecastcity");
                            int cityCode = H2Database.getPrevCityCode(userId);
                            String cityName = H2Database.getPrevCityName(userId);

                            if (cityCode == -1 ||cityName.equals("")) {
                                bot.execute(new SendMessage(userId, "Не найдено предыдущего запроса"));
                                logger.error("В базе данных запросов по городам нет записей");
                            }
                            else {
                                String message = getForecastMessage(String.valueOf(cityCode), "Город: " + cityName);
                                bot.execute(new SendMessage(userId, message));
                                logger.info("Программа успешно отправила прогноз погоды пользователю");
                            }
                            break;
                        }
                        case "/prevforecastgeo": {
                            logger.debug("Программа получила /prevforecastgeo");
                            int locationCode = H2Database.getPrevGeoCode(userId);
                            String locationName = H2Database.getPrevGeoName(userId);

                            if (locationCode == -1 || locationName.equals("")) {
                                bot.execute(new SendMessage(userId, "Не найдено предыдущего запроса"));
                                logger.error("В базе данных запросов по геолокации нет записей");
                            }
                            else {
                                String message = getForecastMessage(String.valueOf(locationCode), "Локация: " + locationName);
                                bot.execute(new SendMessage(userId, message));
                                logger.info("Программа успешно отправила прогноз погоды пользователю");
                            }
                            break;
                        }
                        case "/forecastcityhistory": {
                            logger.debug("Программа получила /forecastcityhistory");
                            String history = H2Database.getCityHistory(userId);
                            bot.execute(new SendMessage(userId, history));
                            logger.info("Программа успешно отправила пользователю историю запросов по городам");
                            cityHistoryChooseOn = true;
                            break;
                        }
                        case "/forecastgeohistory": {
                            logger.debug("Программа получила /forecastgeohistory");
                            String history = H2Database.getGeoHistory(userId);
                            bot.execute(new SendMessage(userId, history));
                            logger.info("Программа успешно отправила пользователю историю запросов по геолокации");
                            geoHistoryChooseOn = true;
                            break;
                        }
                        case "/forecastcityclear": {
                            logger.debug("Программа получила /forecastcityclear");
                            H2Database.clearCityHistory(userId);
                            bot.execute(new SendMessage(userId, "История запросов по городам очищена"));
                            logger.info("Программа успешно очистила историю запросов по городам");
                            break;
                        }
                        case "/forecastgeoclear": {
                            logger.debug("Программа получила /forecastgeoclear");
                            H2Database.clearGeoHistory(userId);
                            bot.execute(new SendMessage(userId, "История запросов по геолокации очищена"));
                            logger.info("Программа успешно очистила историю запросов по геолокации");
                            break;
                        }
                        case "/toggledaily": {
                            logger.debug("Программа получила /toggledaily");
                            break;
                        }
                        case "/setdailytime": {
                            logger.debug("Программа получила /setdailytime");
                            break;
                        }
                        case "/settimezone": {
                            logger.debug("Программа получила /settimezone");
                            break;
                        }
                        default: {
                            // Ввод названия города для получения прогноза на 12 часов
                            if (cityNameInputOn) {
                                String cityName = update.message().text();
                                logger.info("Программа получила от пользователя название города");

                                String contents = HTTPRequest.getCities(cityName);
                                Gson gson = new Gson();
                                locations = gson.fromJson(contents, LocationByCity[].class);
                                logger.debug("Программа получила json сообщение с найденными городами и распрасила его");

                                StringBuilder message = new StringBuilder();
                                if (locations.length == 0) {
                                    message.append("Не найдено городов с таким названием");
                                    logger.info("Пользователь ввёл название несуществующего города");
                                }
                                else {
                                    message.append("Выберите номер города:\n");
                                    for (int i = 0; i < locations.length; i++) {
                                        message.append(i + 1);
                                        message.append(") ");
                                        message.append(locations[i].getLocalizedName());
                                        message.append(", ");
                                        message.append(locations[i].getCountry().getLocalizedName());
                                        message.append(", ");
                                        message.append(locations[i].getAdministrativeArea().getLocalizedName());
                                        message.append("\n");
                                        cityNumberChooseOn = true;
                                    }
                                    logger.debug("Программа составила список городов, основываясь на введённых пользователем данных");
                                }

                                bot.execute(new SendMessage(userId, message.toString()));
                                cityNameInputOn = false;
                            }

                            // Выбор города среди вариантов с одинаковыми названиями
                            else if (cityNumberChooseOn) {
                                logger.info("Программа получила от пользователя номер города");
                                String input = update.message().text();

                                try {
                                    logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
                                    int cityNum = Integer.parseInt(input);
                                    String cityCode = locations[cityNum - 1].getKey();
                                    String cityName =
                                            locations[cityNum - 1].getLocalizedName() + ", " +
                                            locations[cityNum - 1].getCountry().getLocalizedName() + ", " +
                                            locations[cityNum - 1].getAdministrativeArea().getLocalizedName();

                                    bot.execute(new SendMessage(userId, getForecastMessage(cityCode, "Город: " + cityName)));
                                    H2Database.addCityRequest(userId, cityCode, cityName);
                                    logger.info("Программа успешно отправила прогноз погоды пользователю");
                                }
                                catch (NumberFormatException e) {
                                    bot.execute(new SendMessage(userId, "Введено не число"));
                                    logger.error("Программе не удалось преобразовать пользовательский ввод в тип Integer)");
                                    e.printStackTrace();
                                }
                                catch (ArrayIndexOutOfBoundsException e) {
                                    bot.execute(new SendMessage(userId, "Города с таким номером нет в списке"));
                                    logger.error("Города с таким номером нет в массиве");
                                    e.printStackTrace();
                                }

                                cityNumberChooseOn = false;
                            }

                            // Выбор города среди истории запросов
                            else if (cityHistoryChooseOn) {
                                String input = update.message().text();

                                try {
                                    logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
                                    int index = Integer.parseInt(input);
                                    int cityCode = H2Database.getCityCodeByIndex(userId, index);
                                    String cityName = H2Database.getCityNameByIndex(userId, index);

                                    if (cityCode == -1 || cityName.equals("")) {
                                        bot.execute(new SendMessage(userId, "Города с таким номером нет в списке"));
                                        logger.error("Города с таким номером нет в базе данных");
                                    }
                                    else {
                                        String message = getForecastMessage(String.valueOf(cityCode), "Город: " + cityName);
                                        bot.execute(new SendMessage(userId, message));
                                        logger.info("Программа успешно отправила прогноз погоды пользователю");
                                    }
                                }
                                catch (NumberFormatException e) {
                                    bot.execute(new SendMessage(userId, "Введено не число"));
                                    logger.error("Программе не удалось преобразовать пользовательский ввод в тип Integer)");
                                    e.printStackTrace();
                                }

                                cityHistoryChooseOn = false;
                            }

                            // Выбор геолокации среди истории запросов
                            else if (geoHistoryChooseOn) {
                                String input = update.message().text();

                                try {
                                    logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
                                    int index = Integer.parseInt(input);
                                    int locationCode = H2Database.getGeoCodeByIndex(userId, index);
                                    String locationName = H2Database.getGeoNameByIndex(userId, index);

                                    if (locationCode == -1 || locationName.equals("")) {
                                        bot.execute(new SendMessage(userId, "Локации с таким номером нет в списке"));
                                        logger.error("Локации с таким номером нет в базе данных");
                                    }
                                    else {
                                        String message = getForecastMessage(String.valueOf(locationCode), "Локация " + locationName);
                                        bot.execute(new SendMessage(userId, message));
                                        logger.info("Программа успешно отправила прогноз погоды пользователю");
                                    }
                                }
                                catch (NumberFormatException e) {
                                    bot.execute(new SendMessage(userId, "Введено не число"));
                                    logger.error("Программе не удалось преобразовать пользовательский ввод в тип Integer)");
                                    e.printStackTrace();
                                }
                                geoHistoryChooseOn = false;
                            }
                        }
                    }
                }

                // Прогноз погоды на 12 часов по геолокации
                else if (locationInputOn) {
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

                        bot.execute(new SendMessage(userId, message));
                        H2Database.addGeoRequest(userId, location.getKey(), locationName);
                        logger.info("Программа успешно отправила прогноз погоды пользователю");
                    }
                    catch (Exception e) {
                        bot.execute(new SendMessage(userId, "Неверный ввод геолокации"));
                        logger.error("Программе не удалось получить данные геолокации");
                        e.printStackTrace();
                    }

                    locationInputOn = false;
                }
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static String getForecastMessage(String code, String name) {
        logger.debug("Программа в методе getForecastMessage()");

        String contents = HTTPRequest.get12HourForecast(code);
        Gson gson = new Gson();
        HourForecast[] hourForecasts = gson.fromJson(contents, HourForecast[].class);
        logger.debug("Программа получила json сообщение с прогнозом погоды и распрасила его");

        StringBuilder message = new StringBuilder(name);
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
