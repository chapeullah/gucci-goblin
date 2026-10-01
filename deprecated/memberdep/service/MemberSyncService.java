package com.chapeullah.guccigoblin.memberdep.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSyncService {

    private final MemberService memberService;

    @Transactional
    public void sync() {
        var result = memberService.syncMembers();
    }
}