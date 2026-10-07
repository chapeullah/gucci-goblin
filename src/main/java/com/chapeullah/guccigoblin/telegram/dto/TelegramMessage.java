package com.chapeullah.guccigoblin.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessage(
        TelegramChat telegramChat,
        String text) {}