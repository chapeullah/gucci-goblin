package com.chapeullah.guccigoblin.member.dto;

import lombok.NonNull;

public record BuilderBaseLeagueResponse(
        @NonNull Integer id,
        @NonNull String name) {}