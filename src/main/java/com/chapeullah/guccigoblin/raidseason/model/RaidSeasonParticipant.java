package com.chapeullah.guccigoblin.raidseason.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "raid_season_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_raid_season_participants_season_tag",
                columnNames = {"raid_season_id", "tag"}))
@Getter
@Setter
@NoArgsConstructor
public class RaidSeasonParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raid_season_id", nullable = false)
    private RaidSeason raidSeason;

    @Column(name = "tag", nullable = false)
    private String tag;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "attacks", nullable = false)
    private Integer attacks;

    @Column(name = "attack_limit", nullable = false)
    private Integer attackLimit;

    @Column(name = "bonus_attack_limit", nullable = false)
    private Integer bonusAttackLimit;

    @Column(name = "capital_resources_looted", nullable = false)
    private Integer capitalResourcesLooted;

    public RaidSeasonParticipant(
            RaidSeason raidSeason,
            String tag,
            String name,
            Integer attacks,
            Integer attackLimit,
            Integer bonusAttackLimit,
            Integer capitalResourcesLooted) {
        this.raidSeason = raidSeason;
        this.tag = tag;
        this.name = name;
        this.attacks = attacks;
        this.attackLimit = attackLimit;
        this.bonusAttackLimit = bonusAttackLimit;
        this.capitalResourcesLooted = capitalResourcesLooted;
    }

}
