package com.telegramweatherbot.presentation;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.telegramweatherbot.model.Chat;
import com.telegramweatherbot.service.ChatConfig;
import com.telegramweatherbot.service.Utils;
import org.apache.log4j.Logger;

import java.util.HashMap;

public class TelegramWeatherBot {

    private static final Logger logger = Logger.getLogger(TelegramWeatherBot.class);
    private static final HashMap<Long, Chat> chats = new HashMap<>();
    private static TelegramBot bot;
    private TelegramWeatherBot(){}

    public static TelegramBot getBot() { return bot; }

    public static void start() {
        logger.debug("Программа в методе start() класса TelegramWeatherBot");
        String token = Utils.getProperties().getProperty("telegramBotToken");
        bot = new TelegramBot(token);

        //---------------------------------------------Обработка обновлений---------------------------------------------
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                Long id = update.message().from().id();
                if (!chats.containsKey(id))
                {
                    Chat chat = new Chat(id);
                    ChatConfig.config(chat);
                    chats.put(id, chat);
                }
                chats.get(id).processUpdate(update);
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
