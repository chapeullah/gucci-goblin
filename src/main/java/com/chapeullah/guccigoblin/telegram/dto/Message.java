package com.chapeullah.guccigoblin.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
        Chat chat,
        String text) {}