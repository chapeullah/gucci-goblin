package com.chapeullah.guccigoblin.war.dto;

import java.util.List;

public record WarMemberResponse(
        String tag,
        String name,
        Integer townhallLevel,
        Integer mapPosition,
        List<WarAttackResponse> attacks,
        Integer opponentAttacks,
        WarAttackResponse bestOpponentAttack) {
    public WarMemberResponse {
        attacks = attacks == null ? List.of() : attacks;
    }
}
