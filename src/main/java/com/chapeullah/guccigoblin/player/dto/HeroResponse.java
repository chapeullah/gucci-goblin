package com.chapeullah.guccigoblin.player.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HeroResponse(
        String name,
        Integer level,
        Integer maxLevel,
        @JsonProperty("equipment")
        List<HeroEquipmentResponse> heroEquipmentsResponse,
        String village) {}
