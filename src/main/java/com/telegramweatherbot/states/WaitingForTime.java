package com.telegramweatherbot.states;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.telegramweatherbot.service.*;
import org.apache.log4j.Logger;

import java.sql.Time;

public class WaitingForTime extends State {

    private static final Logger logger = Logger.getLogger(WaitingForTime.class);

    public WaitingForTime(Chat chat) {
        super(chat);
    }

    @Override
    public void processUpdate(Update update) {
        logger.debug("Программа в методе processUpdate() класса WaitingForTime");

        String userMessage = update.message().text();
        try {
            Time time = Time.valueOf(userMessage);
            H2Database.getDatabase().setDailyForecastTime(chat.getId(), time);
            chat.getClock().setAlarmTime(time);
            if (chat.getClock().isEnabled()) { chat.getClock().startClock(); }
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), String.format("Время ежедневного прогноза установлено на %s", time)));
            logger.info(String.format("В чате %s установили время ежедневного прогноза на %s", chat.getId(), time));
        }
        catch(Exception e) {
            TelegramWeatherBot.getBot().execute(new SendMessage(chat.getId(), "Время введено неверно"));
            logger.error(e.getMessage(), e);
        }

        chat.setState(new WaitingForCommand(chat));
    }
}
