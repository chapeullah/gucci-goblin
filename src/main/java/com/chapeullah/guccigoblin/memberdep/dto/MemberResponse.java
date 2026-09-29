package com.chapeullah.guccigoblin.memberdep.dto;

import lombok.NonNull;

public record MemberResponse(
        @NonNull String tag,
        @NonNull String name,
        @NonNull String role,
        @NonNull Integer townHallLevel,
        @NonNull Integer expLevel,
        @NonNull Integer donations,
        @NonNull Integer donationsReceived,
        @NonNull Integer builderBaseTrophies,
        @NonNull BuilderBaseLeagueResponse builderBaseLeague,
        @NonNull LeagueTierResponse leagueTier,
        @NonNull Integer clanRank) {}