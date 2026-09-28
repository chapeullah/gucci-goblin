package com.chapeullah.guccigoblin.raidseason.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RaidSeasonResponse(List<Season> items) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Season(
            String state,
            String startTime,
            String endTime,
            Integer capitalTotalLoot,
            Integer raidsCompleted,
            Integer totalAttacks,
            Integer enemyDistrictsDestroyed,
            Integer offensiveReward,
            Integer defensiveReward,
            List<Member> members,
            List<AttackLogEntry> attackLog) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Member(
            String tag,
            String name,
            Integer attacks,
            Integer attackLimit,
            Integer bonusAttackLimit,
            Integer capitalResourcesLooted) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AttackLogEntry(
            Defender defender,
            List<District> districts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Defender(
            String tag,
            String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record District(
            Integer id,
            String name,
            Integer districtHallLevel,
            List<Attack> attacks) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attack(
            Attacker attacker,
            Integer stars,
            Integer destructionPercent) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attacker(
            String tag,
            String name) {}
}