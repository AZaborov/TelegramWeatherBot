package com.telegramweatherbot.service;

import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.states.WaitingForCommand;
import org.apache.log4j.Logger;

import java.sql.Time;

public class ChatConfig {

    private static final Logger logger = Logger.getLogger(ChatConfig.class);

    private ChatConfig(){}

    public static void config(Chat chat) {
        logger.debug("Программа в методе config() класса ChatConfig");

        long id = chat.getId();

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
