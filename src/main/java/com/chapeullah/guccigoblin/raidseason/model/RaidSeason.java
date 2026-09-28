package com.chapeullah.guccigoblin.raidseason.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "raid_seasons",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_raids_clan_start",
                columnNames = {"clan_tag", "start_time"}))
@Getter @Setter
@NoArgsConstructor
public class RaidSeason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clan_tag", nullable = false)
    private String clanTag;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "capital_total_loot", nullable = false)
    private Integer capitalTotalLoot;

    @Column(name = "raids_completed", nullable = false)
    private Integer raidsCompleted;

    @Column(name = "total_attacks", nullable = false)
    private Integer totalAttacks;

    @Column(name = "enemy_districts_destroyed", nullable = false)
    private Integer enemyDistrictsDestroyed;

    @Column(name = "offensive_reward", nullable = false)
    private Integer offensiveReward;

    @Column(name = "defensive_reward", nullable = false)
    private Integer defensiveReward;

    public RaidSeason(
            String clanTag,
            String state,
            Instant startTime,
            Instant endTime,
            Integer capitalTotalLoot,
            Integer raidsCompleted,
            Integer totalAttacks,
            Integer enemyDistrictsDestroyed,
            Integer offensiveReward,
            Integer defensiveReward) {
        this.clanTag = clanTag;
        this.state = state;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capitalTotalLoot = capitalTotalLoot;
        this.raidsCompleted = raidsCompleted;
        this.totalAttacks = totalAttacks;
        this.enemyDistrictsDestroyed = enemyDistrictsDestroyed;
        this.offensiveReward = offensiveReward;
        this.defensiveReward = defensiveReward;
    }

}
