package com.telegramweatherbot.service;

import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.business.WaitingForCommand;
import com.telegramweatherbot.model.Chat;
import com.telegramweatherbot.model.DailyForecastAlarmClock;
import org.apache.log4j.Logger;

import java.sql.Time;

public class ChatConfig {

    private static final Logger logger = Logger.getLogger(ChatConfig.class);

    private ChatConfig(){}

    public static void config(Chat chat) {
        logger.debug("Программа в методе config() класса ChatConfig");

        long id = chat.getId();
        H2Database.addChat(id);

        try {
            Time time = H2Database.getDailyForecastTime(id);
            String name = H2Database.getDailyForecastCityName(id);
            String code = H2Database.getDailyForecastCityCode(id);
            String zone = H2Database.getDailyForecastTimeZone(id);

            DailyForecastAlarmClock clock = new DailyForecastAlarmClock(id, time, name, code, zone);
            chat.setClock(clock);
            chat.setState(new WaitingForCommand(chat));
        }
        catch(Exception e) {
            logger.error(e.getMessage());
        }
    }
}
