package com.telegramweatherbot.model;

import com.pengrad.telegrambot.model.Update;
import com.telegramweatherbot.model.Chat;

public abstract class State {
    protected Chat chat;
    protected abstract void processUpdate(Update update);

    protected State(Chat chat) {
        this.chat = chat;
    }
}
