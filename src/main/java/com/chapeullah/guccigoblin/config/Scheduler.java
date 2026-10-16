package com.chapeullah.guccigoblin.config;

import com.chapeullah.guccigoblin.clan.service.ClanService;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.war.model.War;
import com.chapeullah.guccigoblin.war.service.WarService;
import com.chapeullah.guccigoblin.war.service.WarLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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
        synchronizeClan();
        finishEndedWars();
        synchronizeCurrentWar();
        synchronizeWarLog();
    }

    private void finishEndedWars() {
        try {
            log.info("Finishing ended wars: clanTag=\"{}\"", clanTag);
            List<War> finishedWars = warService.finishEndedWars();
            log.info("Ended wars finishing success: clanTag=\"{}\", finishedWarsCount={}", clanTag, finishedWars.size());
        } catch (Exception e) {
            log.error("Ended wars finishing failed: clanTag=\"{}\"", clanTag, e);
        }
    }

    private void synchronizeClan() {
        try {
            log.info("Synchronizing clan: clanTag=\"{}\"", clanTag);
            Clan clan = clanService.syncClan(clanTag);
            log.info("Clan synchronization success: clanTag=\"{}\", memberCount={}", clanTag, clan.getMemberCount());
        } catch (Exception e) {
            log.error("Clan synchronization failed: clanTag=\"{}\"", clanTag, e);
        }
    }

    private void synchronizeCurrentWar() {
        try {
            log.info("Synchronizing current war: clanTag=\"{}\"", clanTag);
            War war = warService.syncCurrentWar(clanTag).orElse(null);
            if (war == null) {
                log.info("Current war synchronization success: clanTag=\"{}\" - war not found", clanTag);
                return;
            }
            log.info("Current war synchronization success: clanTag=\"{}\", state=\"{}\"", clanTag, war.getState());
        } catch (Exception e) {
            log.error("Current war synchronization failed: clanTag=\"{}\"", clanTag, e);
        }
    }

    private void synchronizeWarLog() {
        try {
            log.info("Synchronizing war log: clanTag=\"{}\"", clanTag);
            warLogService.syncWarLog(clanTag);
            log.info("War log synchronization success: clanTag=\"{}\"", clanTag);
        } catch (Exception e) {
            log.error("War log synchronization failed: clanTag=\"{}\"", clanTag, e);
        }
    }

}
