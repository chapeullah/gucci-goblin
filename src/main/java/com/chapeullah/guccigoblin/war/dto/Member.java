package com.chapeullah.guccigoblin.war.dto;

import java.util.List;

public record Member(
        String tag,
        String name,
        Integer townhallLevel,
        Integer mapPosition,
        List<Attack> attacks,
        Integer opponentAttacks,
        Attack bestOpponentAttack) {
    public Member {
        attacks = attacks == null ? List.of() : attacks;
    }
}