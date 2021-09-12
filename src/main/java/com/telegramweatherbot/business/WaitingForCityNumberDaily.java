package com.telegramweatherbot.business;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.model.Chat;
import com.telegramweatherbot.model.LocationByCity;
import com.telegramweatherbot.model.State;
import com.telegramweatherbot.presentation.TelegramWeatherBot;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class WaitingForCityNumberDaily extends State {

    private static final Logger logger = Logger.getLogger(WaitingForCityNumberDaily.class);

    public WaitingForCityNumberDaily(Chat chat) { super(chat);}

    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForCityNumberDaily");

        String userMessage = update.message().text();
        try {
            logger.warn("Возможно ввести неверные данные (не целое число или номер, которого нет в списке)");
            int cityNum = Integer.parseInt(userMessage);
            ArrayList<LocationByCity> cities = H2Database.getCities(chat.getId());
            String cityName = cities.get(cityNum - 1).getLocalizedName();
            String cityCode = cities.get(cityNum - 1).getKey();
            String timeZone = cities.get(cityNum - 1).getTimeZone().getName();

            H2Database.setDailyForecastCityName(chat.getId(), cityName);
            H2Database.setDailyForecastCityCode(chat.getId(), cityCode);
            H2Database.setDailyForecastTimeZone(chat.getId(), timeZone);

            chat.getClock().setCityName(cityName);
            chat.getClock().setCityCode(cityCode);
            chat.getClock().setTimeZone(timeZone);

            if (chat.getClock().isEnabled()) { chat.getClock().startClock(); }
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), String.format("Город %s установлен для ежедневного прогноза", cityName)));

            logger.info(String.format("В чате %s установили город %s для ежедневного прогноза", chat.getId(), cityName));
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
