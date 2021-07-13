package com.telegramweatherbot;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TelegramWeatherBot {

    private static Boolean cityNameInputOn = false;
    private static Boolean cityNumberChooseOn = false;
    private static LocationByCity[] locations;

    public static void main(String[] args) {
        String telegramBotToken = "1794124946:AAEf-Z74Zgb6yx0aaOw0rQ0BVFpDYO820fA";
        TelegramBot bot = new TelegramBot(telegramBotToken);

        // Обработка обновлений
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                long userId = update.message().from().id();
                String userMessage = update.message().text();

                switch (userMessage) {
                    case "/start": {
                        String message = "Бот для прогноза погоды. Введите /help для получения списка доступных команд.";
                        bot.execute(new SendMessage(userId, message));
                        break;
                    }
                    case "/help": {
                        String message = "/forecasthere - получить прогноз на 12 часов по текущей геолокации " +
                                         "(работает только в мобильной версии)\n" +
                                         "/forecastcity - получить прогноз на 12 часов по выбранному городу\n";
                        bot.execute(new SendMessage(userId, message));
                        break;
                    }

                    // Прогноз погоды на 12 часов по геолокации
                    case "/forecasthere": {
                        //Float latitude = update.message().location().latitude();
                        //Float longitude = update.message().location().longitude();
                        //String contents = HTTPRequest.getLocation(latitude, longitude);
                        //Gson gson = new Gson();
                        //LocationByGeo location = gson.fromJson(contents, LocationByGeo.class);
                        bot.execute(new SendMessage(userId, getForecastMessage("294021")));
                        break;
                    }

                    // Прогноз погоды на 12 часов в определённом городе
                    case "/forecastcity": {
                        bot.execute(new SendMessage(userId, "Введите название города"));
                        cityNameInputOn = true;
                        break;
                    }
                    default: {
                        // Ввод названия города для получения прогноза на 12 часов
                        if (cityNameInputOn) {
                            String cityName = update.message().text();
                            String contents = HTTPRequest.getCities(cityName);
                            Gson gson = new Gson();
                            locations = gson.fromJson(contents, LocationByCity[].class);
                            StringBuilder message = new StringBuilder();

                            if (locations.length == 0) {
                                message.append("Не найдено городов с таким названием");
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
                            }

                            bot.execute(new SendMessage(userId, message.toString()));
                            cityNameInputOn = false;
                        }

                        // Выбор города среди вариантов с одинаковыми названиями
                        else if (cityNumberChooseOn) {
                            String input = update.message().text();

                            try {
                                int cityNum = Integer.parseInt(input);
                                String cityCode = locations[cityNum - 1].getKey();
                                bot.execute(new SendMessage(userId, getForecastMessage(cityCode)));
                            }
                            catch(Exception e) {
                                bot.execute(new SendMessage(userId, "Неверный номер города"));
                                e.printStackTrace();
                            }

                            cityNumberChooseOn = false;
                        }
                    }
                }
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static String getForecastMessage(String code) {
        String contents = HTTPRequest.get12HourForecast(code);
        Gson gson = new Gson();
        HourForecast[] hourForecasts = gson.fromJson(contents, HourForecast[].class);
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

        return message.toString();
    }

}
