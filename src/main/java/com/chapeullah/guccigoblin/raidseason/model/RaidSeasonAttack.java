package com.chapeullah.guccigoblin.raidseason.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "raid_season_attacks")
@Getter @Setter
@NoArgsConstructor
public class RaidSeasonAttack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private RaidSeasonParticipant participant;

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
            String defenderTag,
            String defenderName,
            Integer districtId,
            String districtName,
            Integer districtHallLevel,
            Integer stars,
            Integer destructionPercent) {
        this.participant = participant;
        this.defenderTag = defenderTag;
        this.defenderName = defenderName;
        this.districtId = districtId;
        this.districtName = districtName;
        this.districtHallLevel = districtHallLevel;
        this.stars = stars;
        this.destructionPercent = destructionPercent;
    }
}