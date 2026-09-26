package com.chapeullah.guccigoblin.config;

import com.chapeullah.guccigoblin.member.MemberService;
import com.chapeullah.guccigoblin.player.PlayerService;
import com.chapeullah.guccigoblin.war.WarService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final MemberService memberService;
    private final PlayerService playerService;
    private final WarService warService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sync() {
        memberService   .syncMembers();
        playerService   .syncPlayers();
        warService      .syncWar();
    }

}