package com.chapeullah.guccigoblin.war.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Clan(
        String tag,
        String name,
        Integer clanLevel,
        Integer attacks,
        Integer stars,
        Double destructionPercentage,
        List<Member> members) {}