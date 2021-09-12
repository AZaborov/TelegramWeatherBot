package com.telegramweatherbot.states;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.model.LocationByCity;
import com.telegramweatherbot.presentation.TelegramWeatherBot;
import com.telegramweatherbot.service.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class WaitingForCityNumber12Hour extends State {

    private static final Logger logger = Logger.getLogger(WaitingForCityNumber12Hour.class);

    public WaitingForCityNumber12Hour(Chat chat) {
        super(chat);
    }

    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForCityNumber12Hour");

        String userMessage = update.message().text();
        try {
            logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
            int cityNum = Integer.parseInt(userMessage);
            ArrayList<LocationByCity> cities = H2Database.getCities(chat.getId());
            String cityName = cities.get(cityNum - 1).getLocalizedName();
            String cityCode = cities.get(cityNum - 1).getKey();

            // Получаем прогноз погоды
            String message = Utils.getForecastMessage(cityCode, "Город: " + cityName);

            // Отправляем прогноз погоды в чат в Telegram
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message));

            // Заносим текущий город в историю в базе данных
            H2Database.addCityRequest(chat.getId(), cityCode, cityName);

            logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chat.getId()));

        }
        catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
        }
        catch (NumberFormatException e) {
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Введено не число"));
            logger.error(e.getMessage(), e);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Города с таким номером нет в списке"));
            logger.error(e.getMessage(), e);
        }

        chat.setState(new WaitingForCommand(chat));
    }
}
