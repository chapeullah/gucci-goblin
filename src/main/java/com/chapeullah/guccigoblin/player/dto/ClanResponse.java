package com.chapeullah.guccigoblin.player.dto;

public record ClanResponse(
        String tag,
        String name,
        Integer clanLevel,
        BadgeUrlsResponse badgeUrls) {}
