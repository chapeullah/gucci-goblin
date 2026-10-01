package com.chapeullah.guccigoblin.player.dto;

import com.chapeullah.guccigoblin.builderbaseleague.dto.BuilderBaseLeagueResponse;
import com.chapeullah.guccigoblin.label.dto.LabelResponse;
import com.chapeullah.guccigoblin.leaguetier.dto.LeagueTierResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlayerResponse(
        String tag,
        String name,
        Integer townHallLevel,
        Integer townHallWeaponLevel,
        Integer expLevel,
        Integer trophies,
        Integer bestTrophies,
        Integer warStars,
        Integer attackWins,
        Integer defenseWins,
        Integer builderHallLevel,
        Integer builderBaseTrophies,
        Integer bestBuilderBaseTrophies,
        String role,
        String warPreference,
        Integer donations,
        Integer donationsReceived,
        Integer clanCapitalContributions,
        ClanResponse clan,
        LeagueTierResponse leagueTier,
        BuilderBaseLeagueResponse builderBaseLeague,
        String currentLeagueGroupTag,
        Integer currentLeagueSeasonId,
        String previousLeagueGroupTag,
        Integer previousLeagueSeasonId,
        List<AchievementResponse> achievements,
        PlayerHouseResponse playerHouse,
        List<LabelResponse> labels,
        List<TroopResponse> troops,
        List<HeroResponse> heroes,
        @JsonProperty("heroEquipment")
        List<HeroEquipmentResponse> heroEquipments,
        List<SpellResponse> spells) {}
