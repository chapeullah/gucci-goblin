package com.chapeullah.guccigoblin.config;

import com.chapeullah.guccigoblin.service.GucciService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GucciScheduler {

    private final GucciService gucciService;

    @Scheduled(cron = "0 0 * * * *")
    public void sync() {
        gucciService.synchronize();
    }

}