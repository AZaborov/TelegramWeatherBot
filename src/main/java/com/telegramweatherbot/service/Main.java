package com.telegramweatherbot.service;

import com.telegramweatherbot.presentation.TelegramWeatherBot;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        logger.debug("Программа запущена");
        Utils.readConfigFile();
        TelegramWeatherBot.start();
    }
}
