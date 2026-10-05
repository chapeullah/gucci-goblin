package com.chapeullah.guccigoblin.config;

import com.chapeullah.guccigoblin.player.service.PlayerService;
import com.chapeullah.guccigoblin.raidseason.RaidSeasonService;
import com.chapeullah.guccigoblin.war.WarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final WarService warService;
    private final RaidSeasonService raidSeasonService;
    private final PlayerService playerService;

    @Scheduled(cron = "0 * * * * *")
    public void sync() {
        /*
        try {
            memberSyncService.sync();
        } catch (RuntimeException e) {
            log.error("Members and player events synchronization failed", e);
        }

        try {
            warService.syncWar();
        } catch (RuntimeException e) {
            log.error("War synchronization failed", e);
        }

        try {
            warService.finishEndedWars();
        } catch (RuntimeException e) {
            log.error("Finishing ended wars failed", e);
        }

        try {
            raidSeasonService.syncRaidSeason();
        } catch (RuntimeException e) {
            log.error("Raid season synchronization failed", e);
        }
        */

        try {
            log.info("Sync player #92CPVQC9C");
            playerService.syncPlayer("#92CPVQC9C");
        } catch (RuntimeException e) {
            log.error("Player synchronization failed", e);
        }

    }

}
