package com.telegramweatherbot.states;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.service.*;
import org.apache.log4j.Logger;

public class WaitingForGeoHistory extends State {

    private static final Logger logger = Logger.getLogger(WaitingForGeoHistory.class);

    public WaitingForGeoHistory(Chat chat) {
        super(chat);
    }

    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForGeoHistory");

        String userMessage = update.message().text();
        try {
            logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
            int index = Integer.parseInt(userMessage);
            int locationCode = H2Database.getGeoCodeByIndex(chat.getId(), index);
            String locationName = H2Database.getGeoNameByIndex(chat.getId(), index);

            if (locationCode == -1 || locationName.equals("")) {
                TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Локации с таким номером нет в списке"));
                logger.error(String.format("Локации с таким номером нет в базе данных для чата %s", chat.getId()));
            }
            else {
                String message = Utils.getForecastMessage(String.valueOf(locationCode), "Локация " + locationName);
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
