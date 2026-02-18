package com.chapeullah.GucciGoblin.dto;

import lombok.NonNull;

import java.util.List;

public record ClanMembersResponse(@NonNull List<Member> items) {
    public record Member(
            @NonNull String tag,
            @NonNull String name,
            @NonNull String role,
            @NonNull Integer townHallLevel,
            @NonNull Integer expLevel,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived,
            @NonNull Integer builderBaseTrophies
    ) {}
}
