package com.chapeullah.guccigoblin.member.dto;

import lombok.NonNull;

public record LeagueTierResponse(
        @NonNull Integer id,
        @NonNull String name) {}
