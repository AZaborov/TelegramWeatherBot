package com.telegramweatherbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.telegramweatherbot.dao.H2Database;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.HashMap;

public class TelegramWeatherBot {

    private static final Logger logger = Logger.getLogger(TelegramWeatherBot.class);
    private static final HashMap<Long, Chat> chats = new HashMap<>();
    private static TelegramBot bot;
    private TelegramWeatherBot(){}

    public static TelegramBot getBot() { return bot; }

    public static void main(String[] args) {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        logger.debug("Программа запущена");

        Utils.readConfigFile();
        String token = Utils.getProperties().getProperty("telegramBotToken");
        bot = new TelegramBot(token);

        //---------------------------------------------Обработка обновлений---------------------------------------------
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                Long id = update.message().from().id();
                if (!chats.containsKey(id))
                {
                    chats.put(id, new Chat(id));
                    H2Database.addChat(id);
                }
                chats.get(id).processUpdate(update);
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
