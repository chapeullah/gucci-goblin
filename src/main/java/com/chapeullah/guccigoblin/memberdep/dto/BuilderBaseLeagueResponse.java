package com.chapeullah.guccigoblin.memberdep.dto;

import lombok.NonNull;

public record BuilderBaseLeagueResponse(
        @NonNull Integer id,
        @NonNull String name) {}