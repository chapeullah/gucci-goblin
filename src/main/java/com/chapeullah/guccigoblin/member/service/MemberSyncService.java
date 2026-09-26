package com.chapeullah.guccigoblin.member.service;

import com.chapeullah.guccigoblin.player.PlayerEventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSyncService {

    private final MemberService memberService;
    private final PlayerEventService playerEventService;

    @Transactional
    public void sync() {
        var result = memberService.syncMembers();
        playerEventService.syncEvents(result);
    }
}