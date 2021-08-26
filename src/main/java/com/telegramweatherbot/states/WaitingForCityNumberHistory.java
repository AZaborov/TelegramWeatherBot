package com.telegramweatherbot.states;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.service.*;
import org.apache.log4j.Logger;

public class WaitingForCityNumberHistory extends State {

    private static final Logger logger = Logger.getLogger(WaitingForCityNumberHistory.class);

    public WaitingForCityNumberHistory(Chat chat) {
        super(chat);
    }
    
    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForCityNumberHistory");

        String userMessage = update.message().text();

        try {
            logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
            int index = Integer.parseInt(userMessage);
            int cityCode = H2Database.getCityCodeByIndex(chat.getId(), index);
            String cityName = H2Database.getCityNameByIndex(chat.getId(), index);

            if (cityCode == -1 || cityName.equals("")) {
                TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Города с таким номером нет в списке"));
                logger.error(String.format("Города с таким номером нет в базе данных для чата %s", chat.getId()));
            }
            else {
                String message = Utils.getForecastMessage(String.valueOf(cityCode), "Город: " + cityName);
                TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message));
                logger.info(String.format("Программа успешно отправила прогноз погоды в чат %s", chat.getId()));
            }
        }
        catch (NumberFormatException e) {
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Введено не число"));
            logger.error(e.getMessage(), e);
        }

        chat.setState(new WaitingForCommand(chat));
    }
}
