package com.chapeullah.guccigoblin.war.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClanResponse(
        String tag,
        String name,
        Integer clanLevel,
        Integer attacks,
        Integer stars,
        Double destructionPercentage,
        List<MemberResponse> members) {}
