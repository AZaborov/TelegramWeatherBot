package com.telegramweatherbot.service;

import com.pengrad.telegrambot.model.Update;
import com.telegramweatherbot.dao.H2Database;
import com.telegramweatherbot.states.WaitingForCommand;
import org.apache.log4j.Logger;

import java.sql.Time;

public class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class);
    private final long id;
    private DailyForecastAlarmClock clock;
    private State state;

    public Chat(long id) {
        logger.debug("Программа в конструкторе класса Chat");

        this.id = id;
        Time time = H2Database.getDailyForecastTime(id);
        String name = H2Database.getDailyForecastCityName(id);
        String code = H2Database.getDailyForecastCityCode(id);
        String zone = H2Database.getDailyForecastTimeZone(id);

        clock = new DailyForecastAlarmClock(id, time, name, code, zone);
        setState(new WaitingForCommand(this));
    }

    public long getId() { return id; }

    public DailyForecastAlarmClock getClock() {
        return clock;
    }

    public void setClock(DailyForecastAlarmClock clock) {
        this.clock = clock;
    }

    public void setState(State state) { this.state = state; }

    public void processUpdate(Update update) { state.processUpdate(update); }
}
