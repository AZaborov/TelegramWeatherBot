package com.telegramweatherbot.model;

import com.pengrad.telegrambot.model.Update;
import org.apache.log4j.Logger;

public class Chat {

    private static final Logger logger = Logger.getLogger(Chat.class);
    private final long id;
    private DailyForecastAlarmClock clock;
    private State state;

    public Chat(long id) {
        logger.debug("Программа в конструкторе класса Chat");

        this.id = id;
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
