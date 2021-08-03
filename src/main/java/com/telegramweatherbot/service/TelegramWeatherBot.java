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
                            logger.info("Программа получила /start");
                            String message = "Бот для прогноза погоды. Введите /help для получения списка доступных команд.";
                            bot.execute(new SendMessage(userId, message));
                            break;
                        }
                        case "/help": {
                            logger.info("Программа получила /help");
                            String message =
                                    "/forecastgeo - получить прогноз по выбранной геолокации\n" +
                                    "/forecastcity - получить прогноз по выбранному городу\n" +
                                    "/prevforecastgeo - получить прогноз по предыдущей геолокации\n" +
                                    "/prevforecastcity - получить прогноз по предыдущему городу\n" +
                                    "/forecastgeohistory - посмотреть 10 последних запросов по геолокации и выбрать один из них\n" +
                                    "/forecastcityhistory - посмотреть 10 последних запросов по городу и выбрать один из них\n" +
                                    "/forecastgeoclear - очистить историю запросов по геолокации\n" +
                                    "/forecastcityclear - очистить историю запросов по городу\n" +
                                    "/toggledaily - включить/выключить ежедневный прогноз (по умолчанию выключен)\n" +
                                    "/setdailytime - установить время ежедневного прогноза (по умолчанию 9:00)\n" +
                                    "/settimezone - установить часовой пояс (по умолчанию МСК)\n";
                            bot.execute(new SendMessage(userId, message));
                            break;
                        }
                        case "/forecastcity": {
                            logger.info("Программа получила /forecastcity");
                            bot.execute(new SendMessage(userId, "Введите название города"));
                            cityNameInputOn = true;
                            break;
                        }
                        case "/forecastgeo": {
                            logger.info("Программа получила /forecastgeo");
                            bot.execute(new SendMessage(userId, "Отправьте геолокацию"));
                            locationInputOn = true;
                            break;
                        }
                        case "/prevforecastcity": {
                            int cityCode = H2Database.getPrevCity(userId);
                            if (cityCode == -1) {
                                bot.execute(new SendMessage(userId, "Не найдено предыдущего запроса"));
                            }
                            else {
                                bot.execute(new SendMessage(userId, getForecastMessage(String.valueOf(cityCode))));
                                logger.info("Программа успешно отправила прогноз погоды пользователю");
                            }
                            break;
                        }
                        case "/prevforecastgeo": {
                            int cityCode = H2Database.getPrevGeo(userId);
                            if (cityCode == -1) {
                                bot.execute(new SendMessage(userId, "Не найдено предыдущего запроса"));
                            }
                            else {
                                bot.execute(new SendMessage(userId, getForecastMessage(String.valueOf(cityCode))));
                                logger.info("Программа успешно отправила прогноз погоды пользователю");
                            }
                            break;
                        }
                        case "/forecastcityhistory": {
                            break;
                        }
                        case "/forecastgeohistory": {
                            break;
                        }
                        case "/forecastcityclear": {
                            break;
                        }
                        case "/forecastgeoclear": {
                            break;
                        }
                        case "/toggledaily": {
                            break;
                        }
                        case "/setdailytime": {
                            break;
                        }
                        case "/settimezone": {
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
                                    bot.execute(new SendMessage(userId, getForecastMessage(cityCode)));
                                    H2Database.addCityRequest(userId, cityCode);
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

                        bot.execute(new SendMessage(userId, getForecastMessage(location.getKey())));
                        H2Database.addGeoRequest(userId, location.getKey());
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

    private static String getForecastMessage(String code) {
        logger.debug("Программа в методе формирования прогноза погоды на ближайшие 12 часов");

        String contents = HTTPRequest.get12HourForecast(code);
        Gson gson = new Gson();
        HourForecast[] hourForecasts = gson.fromJson(contents, HourForecast[].class);
        logger.debug("Программа получила json сообщение с прогнозом погоды и распрасила его");

        StringBuilder message = new StringBuilder("Прогноз на ближайшие 12 часов:\n");
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
