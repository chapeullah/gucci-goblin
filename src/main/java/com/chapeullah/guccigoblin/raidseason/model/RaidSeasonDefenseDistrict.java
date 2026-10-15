package com.chapeullah.guccigoblin.raidseason.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "raid_season_defense_districts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RaidSeasonDefenseDistrict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defense_log_entry_id", nullable = false)
    private RaidSeasonDefenseLogEntry defenseLogEntry;

    @Column(name = "district_id", nullable = false)
    private Integer districtId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "district_hall_level", nullable = false)
    private Integer districtHallLevel;

    @Column(name = "destruction_percent", nullable = false)
    private Integer destructionPercent;

    @Column(name = "stars", nullable = false)
    private Integer stars;

    @Column(name = "attack_count", nullable = false)
    private Integer attackCount;

    @Column(name = "total_looted", nullable = false)
    private Integer totalLooted;

    public RaidSeasonDefenseDistrict(
            @NonNull RaidSeasonDefenseLogEntry defenseLogEntry,
            Integer districtId,
            String name,
            Integer districtHallLevel,
            Integer destructionPercent,
            Integer stars,
            Integer attackCount,
            Integer totalLooted) {
        this.defenseLogEntry = defenseLogEntry;
        this.districtId = districtId;
        this.name = name;
        this.districtHallLevel = districtHallLevel;
        this.destructionPercent = destructionPercent;
        this.stars = stars;
        this.attackCount = attackCount;
        this.totalLooted = totalLooted;
    }
}
