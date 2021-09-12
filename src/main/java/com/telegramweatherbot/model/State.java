package com.telegramweatherbot.service;

import com.pengrad.telegrambot.model.Update;

public abstract class State {
    protected Chat chat;
    protected abstract void processUpdate(Update update);

    protected State(Chat chat) {
        this.chat = chat;
    }
}
