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
@Table(name = "raid_season_attack_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RaidSeasonAttackLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raid_season_id", nullable = false)
    private RaidSeason raidSeason;

    @Column(name = "defender_tag", nullable = false)
    private String defenderTag;

    @Column(name = "defender_name", nullable = false)
    private String defenderName;

    @Column(name = "defender_level", nullable = false)
    private Integer defenderLevel;

    @Embedded
    private RaidSeasonBadgeUrls defenderBadgeUrls;

    @Column(name = "attack_count", nullable = false)
    private Integer attackCount;

    @Column(name = "district_count", nullable = false)
    private Integer districtCount;

    @Column(name = "districts_destroyed", nullable = false)
    private Integer districtsDestroyed;

    @OneToMany(mappedBy = "attackLogEntry", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @OrderBy("id ASC")
    private List<RaidSeasonDistrict> districts = new ArrayList<>();

    public RaidSeasonAttackLogEntry(
            @NonNull RaidSeason raidSeason,
            @NonNull String defenderTag,
            @NonNull String defenderName,
            Integer defenderLevel,
            RaidSeasonBadgeUrls defenderBadgeUrls,
            Integer attackCount,
            Integer districtCount,
            Integer districtsDestroyed) {
        this.raidSeason = raidSeason;
        this.defenderTag = defenderTag;
        this.defenderName = defenderName;
        this.defenderLevel = defenderLevel;
        this.defenderBadgeUrls = defenderBadgeUrls;
        this.attackCount = attackCount;
        this.districtCount = districtCount;
        this.districtsDestroyed = districtsDestroyed;
    }
}
