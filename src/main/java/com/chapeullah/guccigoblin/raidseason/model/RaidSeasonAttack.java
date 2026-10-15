package com.chapeullah.guccigoblin.raidseason.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "raid_season_attacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RaidSeasonAttack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "participant_id", nullable = true)
    private RaidSeasonParticipant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raid_season_district_id", nullable = false)
    private RaidSeasonDistrict district;

    @Column(name = "attacker_tag", nullable = false)
    private String attackerTag;

    @Column(name = "attacker_name", nullable = false)
    private String attackerName;

    @Column(name = "defender_tag", nullable = false)
    private String defenderTag;

    @Column(name = "defender_name", nullable = false)
    private String defenderName;

    @Column(name = "district_id", nullable = false)
    private Integer districtId;

    @Column(name = "district_name", nullable = false)
    private String districtName;

    @Column(name = "district_hall_level", nullable = false)
    private Integer districtHallLevel;

    @Column(name = "stars", nullable = false)
    private Integer stars;

    @Column(name = "destruction_percent", nullable = false)
    private Integer destructionPercent;

    public RaidSeasonAttack(
            RaidSeasonParticipant participant,
            @NonNull RaidSeasonDistrict district,
            @NonNull String attackerTag,
            @NonNull String attackerName,
            Integer stars,
            Integer destructionPercent) {
        this.participant = participant;
        this.district = district;
        this.attackerTag = attackerTag;
        this.attackerName = attackerName;
        this.defenderTag = district.getAttackLogEntry().getDefenderTag();
        this.defenderName = district.getAttackLogEntry().getDefenderName();
        this.districtId = district.getDistrictId();
        this.districtName = district.getName();
        this.districtHallLevel = district.getDistrictHallLevel();
        this.stars = stars;
        this.destructionPercent = destructionPercent;
    }

}
