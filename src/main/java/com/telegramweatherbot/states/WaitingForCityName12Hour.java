package com.telegramweatherbot.states;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.model.LocationByCity;
import com.telegramweatherbot.service.AccuWeatherRequests;
import com.telegramweatherbot.service.Chat;
import com.telegramweatherbot.service.State;
import com.telegramweatherbot.service.TelegramWeatherBot;
import org.apache.log4j.Logger;

public class WaitingForCityName12Hour extends State {

    private static final Logger logger = Logger.getLogger(WaitingForCityName12Hour.class);

    public WaitingForCityName12Hour(Chat chat) {
        super(chat);
    }

    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForCityName12Hour");

        // После ввода названия города получаем список городов с одинаковыми названиеми
        String userCityRequest = update.message().text();

        LocationByCity[] cities = AccuWeatherRequests.getCities(userCityRequest);
        logger.debug(String.format("Программа получила от чата %s json сообщение с найденными городами и распрасила его", chat.getId()));

        StringBuilder message = new StringBuilder();
        if (cities.length == 0) {
            message.append("Не найдено городов с таким названием");
            logger.error(String.format("В чате %s ввели название несуществующего города", chat.getId()));
            chat.setState(new WaitingForCommand(chat));
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
            }
            chat.setState(new WaitingForCityNumber12Hour(chat));
            logger.debug(String.format("Программа составила список городов, основываясь на введённых в чате %s данных", chat.getId()));
            H2Database.addCities(chat.getId(), cities);
        }
        TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message.toString()));
    }
}
