package com.chapeullah.guccigoblin.player.dto;

public record AchievementResponse(
        String name,
        Integer stars,
        Integer value,
        Integer target,
        String info,
        String completionInfo,
        String village) {}
