package com.chapeullah.guccigoblin.clan.dto;

public record ClanMemberResponse(
        String tag,
        ClanLeagueResponse league,
        Integer clanRank,
        Integer previousClanRank) {
}
