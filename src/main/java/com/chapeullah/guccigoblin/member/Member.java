package com.chapeullah.guccigoblin.member;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.clan.model.Clan;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @Column(name = "tag",
            nullable = false)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clan_tag", nullable = false)
    private Clan clan;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "role",
            nullable = false)
    private String role;

    @Column(name = "town_hall_level",
            nullable = false)
    private Integer townHallLevel;

    @Column(name = "exp_level",
            nullable = false)
    private Integer expLevel;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = true)
    @JoinColumn(
            name = "league_tier_id",
            nullable = true)
    private LeagueTier leagueTier;

    @Column(name = "trophies", nullable = true)
    private Integer trophies;

    @Column(name = "builder_base_trophies", nullable = true)
    private Integer builderBaseTrophies;

    @Column(name = "clan_rank", nullable = false)
    private Integer clanRank;

    @Column(name = "previous_clan_rank", nullable = true)
    private Integer previousClanRank;

    @Column(name = "donations",
            nullable = false)
    private Integer donations;

    @Column(name = "donations_received",
            nullable = false)
    private Integer donationsReceived;

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<MemberHouseElement> memberHouseElements = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "builder_base_league_id", nullable = true)
    private BuilderBaseLeague builderBaseLeague;

    public Member(
            @NonNull String tag,
            @NonNull Clan clan,
            @NonNull String name,
            @NonNull String role,
            @NonNull Integer townHallLevel,
            @NonNull Integer expLevel,
            LeagueTier leagueTier,
            Integer trophies,
            Integer builderBaseTrophies,
            @NonNull Integer clanRank,
            Integer previousClanRank,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived,
            BuilderBaseLeague builderBaseLeague) {
        if (tag.isBlank()) {
            throw new IllegalArgumentException("Member tag must not be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Member name must not be blank");
        }
        if (role.isBlank()) {
            throw new IllegalArgumentException("Member role must not be blank");
        }
        if (townHallLevel < 1) {
            throw new IllegalArgumentException("Member town hall level must be positive");
        }
        if (expLevel < 1) {
            throw new IllegalArgumentException("Member experience level must be positive");
        }
        if (trophies != null && trophies < 0) {
            throw new IllegalArgumentException("Member trophies must not be negative");
        }
        if (builderBaseTrophies != null && builderBaseTrophies < 0) {
            throw new IllegalArgumentException("Member builder base trophies must not be negative");
        }
        if (clanRank < 1 || clanRank > 50) {
            throw new IllegalArgumentException("Member clan rank must be between 1 and 50");
        }
        if (previousClanRank != null && (previousClanRank < 1 || previousClanRank > 50)) {
            throw new IllegalArgumentException("Previous member clan rank must be between 1 and 50");
        }
        if (donations < 0) {
            throw new IllegalArgumentException("Member donations must not be negative");
        }
        if (donationsReceived < 0) {
            throw new IllegalArgumentException("Member received donations must not be negative");
        }
        this.tag = tag;
        this.clan = clan;
        this.name = name;
        this.role = role;
        this.townHallLevel = townHallLevel;
        this.expLevel = expLevel;
        this.leagueTier = leagueTier;
        this.trophies = trophies;
        this.builderBaseTrophies = builderBaseTrophies;
        this.clanRank = clanRank;
        this.previousClanRank = previousClanRank;
        this.donations = donations;
        this.donationsReceived = donationsReceived;
        this.builderBaseLeague = builderBaseLeague;
    }

}
