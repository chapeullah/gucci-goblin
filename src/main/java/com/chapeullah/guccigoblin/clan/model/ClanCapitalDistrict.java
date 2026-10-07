package com.chapeullah.guccigoblin.clan.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(
        name = "clan_capital_districts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_clan_capital_districts_clan_tag_district_id",
                columnNames = {"clan_tag", "district_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClanCapitalDistrict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clan_tag", nullable = false)
    private ClanCapital clanCapital;

    @Column(name = "district_id",
            nullable = false)
    private Long districtId;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "district_hall_level",
            nullable = false)
    private Integer districtHallLevel;

    public ClanCapitalDistrict(
            @NonNull ClanCapital clanCapital,
            @NonNull Long districtId,
            @NonNull String name,
            @NonNull Integer districtHallLevel) {
        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "District name must not be blank");
        }
        if (districtHallLevel < 1) {
            throw new IllegalArgumentException(
                    "District hall level must be at least 1");
        }
        this.clanCapital = clanCapital;
        this.districtId = districtId;
        this.name = name;
        this.districtHallLevel = districtHallLevel;
    }

    public void updateFrom(
            @NonNull ClanCapitalDistrict source) {
        if (!this.clanCapital.getClanTag().equals(source.clanCapital.getClanTag())) {
            throw new IllegalArgumentException("Clan capital district clan tag mismatch");
        }
        if (!this.districtId.equals(source.districtId)) {
            throw new IllegalArgumentException("Clan capital district id mismatch");
        }
        this.name = source.name;
        this.districtHallLevel = source.districtHallLevel;
    }

}
