package com.telegramweatherbot.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

@Configuration
public class ServiceConfig {

    @Bean
    H2Database h2Database() {
        return new H2Database();
    }

    @Bean
    AccuWeatherRequests accuWeatherRequests() {
        return new AccuWeatherRequests();
    }

    @Bean
    Utils utils() {
        return new Utils();
    }

    @Bean
    @Scope("prototype")
    Chat chat(long id) { return new Chat(id); }
}
