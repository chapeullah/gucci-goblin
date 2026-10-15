package com.chapeullah.guccigoblin.raidseason.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "raid_season_defense_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RaidSeasonDefenseLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raid_season_id", nullable = false)
    private RaidSeason raidSeason;

    @Column(name = "attacker_tag", nullable = false)
    private String attackerTag;

    @Column(name = "attacker_name", nullable = false)
    private String attackerName;

    @Column(name = "attacker_level", nullable = false)
    private Integer attackerLevel;

    @Embedded
    private RaidSeasonBadgeUrls attackerBadgeUrls;

    @Column(name = "attack_count", nullable = false)
    private Integer attackCount;

    @Column(name = "district_count", nullable = false)
    private Integer districtCount;

    @Column(name = "districts_destroyed", nullable = false)
    private Integer districtsDestroyed;

    @OneToMany(mappedBy = "defenseLogEntry", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("id ASC")
    private List<RaidSeasonDefenseDistrict> districts = new ArrayList<>();

    public RaidSeasonDefenseLogEntry(
            @NonNull RaidSeason raidSeason,
            @NonNull String attackerTag,
            @NonNull String attackerName,
            Integer attackerLevel,
            RaidSeasonBadgeUrls attackerBadgeUrls,
            Integer attackCount,
            Integer districtCount,
            Integer districtsDestroyed) {
        this.raidSeason = raidSeason;
        this.attackerTag = attackerTag;
        this.attackerName = attackerName;
        this.attackerLevel = attackerLevel;
        this.attackerBadgeUrls = attackerBadgeUrls;
        this.attackCount = attackCount;
        this.districtCount = districtCount;
        this.districtsDestroyed = districtsDestroyed;
    }
}
