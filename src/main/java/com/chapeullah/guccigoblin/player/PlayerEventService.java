package com.chapeullah.guccigoblin.player;

import com.chapeullah.guccigoblin.member.dto.MemberSnapshot;
import com.chapeullah.guccigoblin.member.dto.MemberSyncResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerEventService {

    private final PlayerEventRepository playerEventRepository;

    public void syncEvents(@NonNull MemberSyncResult result) {
        List<PlayerEvent> events = new ArrayList<>();
        for (MemberSnapshot member : result.joined()) {
            events.add(PlayerEvent.joined(member.tag(), member.name()));
        }
        for (MemberSnapshot member : result.left()) {
            events.add(PlayerEvent.left(member.tag(), member.name()));
        }
        playerEventRepository.saveAll(events);

        log.info("Player events saved. Events: {}", events.size());
    }

}
