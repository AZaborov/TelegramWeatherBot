package com.telegramweatherbot.states;

import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.model.LocationByGeo;
import com.telegramweatherbot.service.*;
import org.apache.log4j.Logger;

public class WaitingForGeo extends State {

    private static final Logger logger = Logger.getLogger(WaitingForGeo.class);

    public WaitingForGeo(Chat chat) {
        super(chat);
    }

    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForGeo");

        try {
            logger.warn("Присланное пользователем сообщение может быть не геолокацией");
            Float latitude = update.message().location().latitude();
            Float longitude = update.message().location().longitude();


            LocationByGeo location =AccuWeatherRequests.getLocation(latitude, longitude);

            logger.debug("Программа получила json сообщение с найденной локацией и распрасила его");

            String locationName = location.getLocalizedName() + ", " + location.getAdministrativeArea().getLocalizedName();
            String message = Utils.getForecastMessage(location.getKey(), "Локация: " + locationName);

            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), message));
            H2Database.addGeoRequest(chat.getId(), location.getKey(), locationName);
            logger.info("Программа успешно отправила прогноз погоды пользователю");
        }
        catch (Exception e) {
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Неверный ввод геолокации"));
            logger.error(e.getMessage(), e);
        }

        chat.setState(new WaitingForCommand(chat));
    }
}
