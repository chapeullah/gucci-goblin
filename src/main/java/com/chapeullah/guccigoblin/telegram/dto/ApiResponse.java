package com.chapeullah.guccigoblin.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponse<T>(
        boolean ok,
        T result,
        String description) {}