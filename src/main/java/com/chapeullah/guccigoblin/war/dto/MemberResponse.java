package com.chapeullah.guccigoblin.war.dto;

import java.util.List;

public record MemberResponse(
        String tag,
        String name,
        Integer townhallLevel,
        Integer mapPosition,
        List<AttackResponse> attacks,
        Integer opponentAttacks,
        AttackResponse bestOpponentAttack) {
    public MemberResponse {
        attacks = attacks == null ? List.of() : attacks;
    }
}
