package com.chapeullah.guccigoblin.war.dto;

import java.util.List;

public record WarMemberResponse(
        String tag,
        String name,
        Integer townHallLevel,
        Integer mapPosition,
        List<WarAttackResponse> attacks,
        Integer opponentAttacks,
        WarAttackResponse bestOpponentAttack) {}
