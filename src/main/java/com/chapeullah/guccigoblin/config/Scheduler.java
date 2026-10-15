package com.chapeullah.guccigoblin.config;

import com.chapeullah.guccigoblin.clan.service.ClanService;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.player.service.PlayerService;
import com.chapeullah.guccigoblin.raidseason.RaidSeasonService;
import com.chapeullah.guccigoblin.war.service.WarService;
import com.chapeullah.guccigoblin.warlog.WarLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final WarLogService warLogService;
    private final WarService warService;
    private final ClanService clanService;

    @Value("${coc.clan-tag}")
    private String clanTag;

    @Scheduled(cron = "0 * * * * *")
    public void sync() {
        try {
            warService.finishEndedWars();
        } catch (Exception e) {
            log.error("Finishing ended wars failed", e);
        }

        try {
            warService.syncCurrentWar(clanTag);
        } catch (RuntimeException e) {
            log.error("War synchronization failed", e);
        }

        try {
            log.info("Sync clan: clanTag=\"{}\"", clanTag);
            Clan clan = clanService.syncClan(clanTag);
            log.info("Sync clan success: clanTag=\"{}\" memberCount={}", clanTag, clan.getMemberCount());
        } catch (Exception e) {
            log.error("Sync clan failed: clanTag=\"{}\"", clanTag, e);
        }

        try {
            warLogService.syncWarLog(clanTag);
        } catch (Exception e) {
            log.error("War log synchronization failed", e);
        }

    }

}
