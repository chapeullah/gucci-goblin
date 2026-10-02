package com.chapeullah.guccigoblin.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnvironmentVariablesValidator {

    private final Environment environment;

    List<String> environmentVariables = List.of(
            "COC_API_BASE_URL",
            "COC_API_TOKEN",
            "COC_CLAN_TAG",
            "TELEGRAM_BOT_TOKEN");

    @PostConstruct
    public void validate() {
        environmentVariables.forEach(this::requireValue);
    }

    private void requireValue(String name) {
        String value = environment.getProperty(name);
        if (value == null) {
            throw new IllegalStateException("Required environment variable is not set: " + name);
        }
        if (value.isBlank()) {
            throw new IllegalStateException("Required environment variable is blank: " + name);
        }
    }

}
