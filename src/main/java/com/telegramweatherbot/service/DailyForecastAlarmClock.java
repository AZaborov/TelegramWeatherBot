package com.telegramweatherbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.Logger;

import java.sql.Time;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;

public class DailyForecastAlarmClock {

    private static final Logger logger = Logger.getLogger(DailyForecastAlarmClock.class);

    private Timer timer;
    private final long chatId;
    private final TelegramBot bot;
    private Time alarmTime;
    private String cityName;
    private String cityCode;
    private String timeZone;

    public Time getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Time alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public DailyForecastAlarmClock(long chatId, TelegramBot bot, Time alarmTime, String cityName, String cityCode, String timeZone) {
        logger.debug(String.format("Создан таймер для чата %s", chatId));
        this.chatId = chatId;
        this.bot = bot;
        this.alarmTime = alarmTime;
        this.cityName = cityName;
        this.cityCode = cityCode;
        this.timeZone = timeZone;
    }

    public void startClock() {
        logger.info(String.format("Таймер чата %s запущен", chatId));

        try {
            timer = new Timer();
            LocalTime startTime = LocalTime.now(ZoneId.of(timeZone));
            LocalTime endTime = LocalTime.of(alarmTime.getHours(), alarmTime.getMinutes(), alarmTime.getSeconds());
            long delay = startTime.until(endTime, ChronoUnit.MILLIS);
            if (delay < 0) delay *= -1;
            long period = 86400000;

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String prevMessage = String.format("Ежедневый прогноз для города %s:", cityName);
                    bot.execute(new SendMessage(chatId, TelegramWeatherBot.getForecastMessage(cityCode, prevMessage)));
                }
            }, delay, period);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void stopClock() {
        timer.cancel();
        logger.info(String.format("Таймер чата %s остановлен", chatId));
    }
}
