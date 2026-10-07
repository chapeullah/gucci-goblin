package com.chapeullah.guccigoblin.clan.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clan_capitals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClanCapital {

    @Id
    @Column(name = "clan_tag",
            nullable = false)
    private String clanTag;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "clan_tag",
            nullable = false)
    private Clan clan;

    @Column(name = "capital_hall_level",
            nullable = false)
    private Integer capitalHallLevel;

    @Column(name = "clan_gold_sink_total",
            nullable = true)
    private Long clanGoldSinkTotal;

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "clanCapital",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ClanCapitalDistrict> districts = new ArrayList<>();

    public ClanCapital(
            @NonNull Clan clan,
            @NonNull Integer capitalHallLevel,
            Long clanGoldSinkTotal) {
        if (capitalHallLevel < 1) {
            throw new IllegalArgumentException("Clan capital hall level must be at least 1");
        }
        if (clanGoldSinkTotal != null && clanGoldSinkTotal < 0) {
            throw new IllegalArgumentException("Clan gold sink total must not be negative");
        }
        this.clanTag = clan.getTag();
        this.clan = clan;
        this.capitalHallLevel = capitalHallLevel;
        this.clanGoldSinkTotal = clanGoldSinkTotal;
    }

    public void updateFrom(@NonNull ClanCapital source) {
        if (!this.clanTag.equals(source.clanTag)) {
            throw new IllegalArgumentException("Clan capital clan tag mismatch");
        }
        this.capitalHallLevel = source.capitalHallLevel;
        this.clanGoldSinkTotal = source.clanGoldSinkTotal;
    }

    public void addDistrict(@NonNull ClanCapitalDistrict district) {
        if (!this.clanTag.equals(district.getClanCapital().getClanTag())) {
            throw new IllegalArgumentException("Clan capital district clan tag mismatch");
        }
        districts.add(district);
    }

    public void removeDistrict(ClanCapitalDistrict district) {
        districts.remove(district);
    }

}
