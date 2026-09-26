package com.chapeullah.guccigoblin.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StartupConfig {

    private final Scheduler scheduler;

    @Bean
    public ApplicationRunner initialize() {
        return args -> scheduler.sync();
    }

}
