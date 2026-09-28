package com.chapeullah.guccigoblin.config;

import com.chapeullah.guccigoblin.member.service.MemberSyncService;
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

    private final MemberSyncService memberSyncService;
    private final WarService warService;
    private final RaidSeasonService raidSeasonService;

    @Scheduled(cron = "0 * * * * *")
    public void sync() {
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
    }

}
