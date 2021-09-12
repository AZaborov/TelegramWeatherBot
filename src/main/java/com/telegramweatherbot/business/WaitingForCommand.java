package com.telegramweatherbot.business;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.model.Chat;
import com.telegramweatherbot.model.State;
import com.telegramweatherbot.presentation.TelegramWeatherBot;
import com.telegramweatherbot.service.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class WaitingForCommand extends State {

    private static final Logger logger = Logger.getLogger(WaitingForCommand.class);

    public WaitingForCommand(Chat chat) {
        super(chat);
    }

    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForCommand");

        String userMessage = update.message().text();
        switch (userMessage) {
            case "/start": start(); break;
            case "/help": help(); break;
            case "/forecastcity": forecastCity(); break;
            case "/forecastgeo": forecastGeo(); break;
            case "/prevforecastcity": prevForecastCity(); break;
            case "/prevforecastgeo": prevForecastGeo(); break;
            case "/forecastcityhistory": forecastCityHistory(); break;
            case "/forecastgeohistory": forecastGeoHistory(); break;
            case "/forecastcityclear": forecastCityClear(); break;
            case "/forecastgeoclear": forecastGeoClear(); break;
            case "/toggledaily": toggleDaily(); break;
            case "/setdailytime": setDailyTime(); break;
            case "/setdailycity": setDailyCity(); break;
        }
    }

    private void start() {
        logger.debug("Программа в методе start() класса WaitingForCommand");

        String message = "Бот для прогноза погоды. Введите /help для получения списка доступных команд";
        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message));
    }

    private void help() {
        logger.debug("Программа в методе help() класса WaitingForCommand");

        StringBuilder message;
        message = new StringBuilder();
        message.append("/forecastcity - получить прогноз по выбранному городу\n");
        message.append("/forecastgeo - получить прогноз по выбранной геолокации\n");
        message.append("/prevforecastcity - получить прогноз по предыдущему городу\n");
        message.append("/prevforecastgeo - получить прогноз по предыдущей геолокации\n");
        message.append("/forecastcityhistory - посмотреть историю запросов по городу и выбрать один из них\n");
        message.append("/forecastgeohistory - посмотреть историю запросов по геолокации и выбрать один из них\n");
        message.append("/forecastcityclear - очистить историю запросов по городу\n");
        message.append("/forecastgeoclear - очистить историю запросов по геолокации\n");
        message.append("/toggledaily - включить/выключить ежедневный прогноз (по умолчанию выключен)\n");
        message.append("/setdailytime - установить время ежедневного прогноза (по умолчанию 9:00)\n");
        message.append("/setdailycity - установить город ежедневного прогноза (по умолчанию Москва)\n");

        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message.toString()));
    }

    private void forecastCity() {
        logger.debug("Программа в методе forecastCity() класса WaitingForCommand");

        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Введите название города"));
        chat.setState(new WaitingForCityName12Hour(chat));
    }

    private void forecastGeo() {
        logger.debug("Программа в методе forecastGeo() класса WaitingForCommand");

        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Отправьте геолокацию"));
        chat.setState(new WaitingForGeo(chat));
    }

    private void prevForecastCity() {
        logger.debug("Программа в методе prevForecastCity() класса WaitingForCommand");

        int cityCode = H2Database.getPrevCityCode(chat.getId());
        String cityName = H2Database.getPrevCityName(chat.getId());

        if (cityCode == -1 ||cityName.equals("")) {
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Не найдено предыдущего запроса"));
            logger.error(String.format("В базе данных запросов по городам для чата %s нет записей", chat.getId()));
        }
        else {
            String message = Utils.getForecastMessage(String.valueOf(cityCode), "Город: " + cityName);
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message));
            logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chat.getId()));
        }
    }

    private void prevForecastGeo() {
        logger.debug("Программа в методе prevForecastGeo() класса WaitingForCommand");

        int locationCode = H2Database.getPrevGeoCode(chat.getId());
        String locationName = H2Database.getPrevGeoName(chat.getId());

        if (locationCode == -1 || locationName.equals("")) {
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Не найдено предыдущего запроса"));
            logger.error(String.format("В базе данных запросов по геолокации для чата %s нет записей", chat.getId()));
        }
        else {
            String message = Utils.getForecastMessage(String.valueOf(locationCode), "Локация: " + locationName);
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message));
            logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chat.getId()));
        }
    }

    private void forecastCityHistory() {
        logger.debug("Программа в методе forecastCityHistory() класса WaitingForCommand");

        StringBuilder message = new StringBuilder();
        ArrayList<String> history = H2Database.getCityHistory(chat.getId());

        if (history.isEmpty()) {
            message.append("История запросов по городам пуста");
            chat.setState(new WaitingForCommand(chat));
        }
        else {
            message.append("История запросов по городам:\n");
            message.append(Utils.formatHistory(history));
            message.append("\nВыберите номер города:");
            chat.setState(new WaitingForCityNumberHistory(chat));
            logger.info(String.format("Программа успешно отправила историю запросов по городам в чат %s", chat.getId()));
        }

        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message.toString()));
    }

    private void forecastGeoHistory() {
        logger.debug("Программа в методе forecastGeoHistory() класса WaitingForCommand");

        StringBuilder message = new StringBuilder();
        ArrayList<String> history = H2Database.getGeoHistory(chat.getId());

        if (history == null || history.isEmpty()) {
            message.append("История запросов по геолокации пуста");
            chat.setState(new WaitingForCommand(chat));
        }
        else {
            message.append("История запросов по геолокации:\n");
            message.append(Utils.formatHistory(history));
            message.append("\nВыберите номер геолокации:");
            chat.setState(new WaitingForGeoHistory(chat));
            logger.info(String.format("Программа успешно отправила историю запросов по геолокации в чат %s", chat.getId()));
        }

        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message.toString()));
    }

    private void forecastCityClear() {
        logger.debug("Программа в методе forecastCityClear() класса WaitingForCommandState");

        H2Database.clearCityHistory(chat.getId());
        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "История запросов по городам очищена"));
        logger.info(String.format("Программа успешно очистила историю запросов по городам для чата %s", chat.getId()));
    }

    private void forecastGeoClear() {
        logger.debug("Программа в методе forecastGeoClear() класса WaitingForCommandState");

        H2Database.clearGeoHistory(chat.getId());
        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "История запросов по геолокации очищена"));
        logger.info(String.format("Программа успешно очистила историю запросов по геолокации для чата %s", chat.getId()));
    }

    private void toggleDaily() {
        logger.debug("Программа в методе toggleDaily() класса WaitingForCommandState");

        // Прогноз включён, выключаем
        if (H2Database.dailyForecastOn(chat.getId())) {
            chat.getClock().stopClock();
            chat.getClock().setEnabled(false);

            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Ежедневый прогноз выключен"));
            logger.info(String.format("В чате %s выключен ежедневый прогноз", chat.getId()));
            H2Database.setDailyForecast(chat.getId(), 0);
        }
        // Прогноз выключен, включаем
        else {
            chat.getClock().startClock();
            chat.getClock().setEnabled(true);

            StringBuilder message;
            message = new StringBuilder();
            message.append("Ежедневый прогноз включён\n");
            message.append("Время: ");
            message.append(chat.getClock().getAlarmTime());
            message.append("\nГород: ");
            message.append(chat.getClock().getCityName());

            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message.toString()));
            logger.info(String.format("В чате %s включён ежедневый прогноз", chat.getId()));
            H2Database.setDailyForecast(chat.getId(), 1);
        }
    }

    private void setDailyTime() {
        logger.debug("Программа в методе setDailyTime() класса WaitingForCommandState");

        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Введите время для ежедневного прогноза (в формате hh:mm:ss):"));
        chat.setState(new WaitingForTime(chat));
    }

    private void setDailyCity() {
        logger.debug("Программа в методе setDailyCity() класса WaitingForCommandState");

        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Введите город для ежедневного прогноза:"));
        chat.setState(new WaitingForCityNameDaily(chat));
    }
}
