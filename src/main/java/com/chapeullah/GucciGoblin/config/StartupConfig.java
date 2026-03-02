package com.chapeullah.GucciGoblin.config;

import com.chapeullah.GucciGoblin.service.GucciService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StartupConfig {

    private final GucciService gucciService;

    @Bean
    public ApplicationRunner initialize() { return args -> gucciService.synchronize(); }

}
