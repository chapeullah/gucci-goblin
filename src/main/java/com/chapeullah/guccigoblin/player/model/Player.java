package com.chapeullah.guccigoblin.player.model;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.clan.Clan;
import com.chapeullah.guccigoblin.label.player.PlayerLabel;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    @Column(name = "tag",
            nullable = false)
    private String tag;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "town_hall_level",
            nullable = false)
    private Integer townHallLevel;

    @Column(name = "town_hall_weapon_level",
            nullable = false)
    private Integer townHallWeaponLevel;

    @Column(name = "exp_level",
            nullable = false)
    private Integer expLevel;

    @Column(name = "trophies",
            nullable = false)
    private Integer trophies;

    @Column(name = "best_trophies",
            nullable = false)
    private Integer bestTrophies;

    @Column(name = "war_stars",
            nullable = false)
    private Integer warStars;

    @Column(name = "attack_wins",
            nullable = false)
    private Integer attackWins;

    @Column(name = "defense_wins",
            nullable = false)
    private Integer defenseWins;

    @Column(name = "builder_hall_level",
            nullable = false)
    private Integer builderHallLevel;

    @Column(name = "builder_base_trophies",
            nullable = false)
    private Integer builderBaseTrophies;

    @Column(name = "best_builder_base_trophies",
            nullable = false)
    private Integer bestBuilderBaseTrophies;

    @Column(name = "role",
            nullable = true)
    private String role;

    @Column(name = "war_preference",
            nullable = false)
    private String warPreference;

    @Column(name = "donations",
            nullable = false)
    private Integer donations;

    @Column(name = "donations_received",
            nullable = false)
    private Integer donationsReceived;

    @Column(name = "clan_capital_contributions",
            nullable = false)
    private Integer clanCapitalContributions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clan_tag", nullable = true)
    private Clan clan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_tier_id", nullable = false)
    private LeagueTier leagueTier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "builder_base_league_id", nullable = false)
    private BuilderBaseLeague builderBaseLeague;

    @Column(name = "current_league_group_tag")
    private String currentLeagueGroupTag;

    @Column(name = "current_league_season_id")
    private Integer currentLeagueSeasonId;

    @Column(name = "previous_league_group_tag")
    private String previousLeagueGroupTag;

    @Column(name = "previous_league_season_id")
    private Integer previousLeagueSeasonId;

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Achievement> achievements = new ArrayList<>();

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<HouseElement> houseElements = new ArrayList<>();

    @SuppressWarnings("FieldMayBeFinal")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "player_label_links",
            joinColumns = @JoinColumn(name = "player_tag", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "label_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_player_label_links_player_label",
                    columnNames = {"player_tag", "label_id"}))
    private List<PlayerLabel> labels = new ArrayList<>();

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Troop> troops = new ArrayList<>();

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Hero> heroes = new ArrayList<>();

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<HeroEquipment> heroEquipments = new ArrayList<>();

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Spell> spells = new ArrayList<>();

    public Player(
            @NonNull String tag,
            @NonNull String name,
            @NonNull Integer townHallLevel,
            @NonNull Integer townHallWeaponLevel,
            @NonNull Integer expLevel,
            @NonNull Integer trophies,
            @NonNull Integer bestTrophies,
            @NonNull Integer warStars,
            @NonNull Integer attackWins,
            @NonNull Integer defenseWins,
            @NonNull Integer builderHallLevel,
            @NonNull Integer builderBaseTrophies,
            @NonNull Integer bestBuilderBaseTrophies,
            String role,
            @NonNull String warPreference,
            @NonNull Integer donations,
            @NonNull Integer donationsReceived,
            @NonNull Integer clanCapitalContributions,
            Clan clan,
            LeagueTier leagueTier,
            BuilderBaseLeague builderBaseLeague,
            String currentLeagueGroupTag,
            Integer currentLeagueSeasonId,
            String previousLeagueGroupTag,
            Integer previousLeagueSeasonId,
            List<PlayerLabel> labels) {
        this.tag = tag;
        this.name = name;
        this.townHallLevel = townHallLevel;
        this.townHallWeaponLevel = townHallWeaponLevel;
        this.expLevel = expLevel;
        this.trophies = trophies;
        this.bestTrophies = bestTrophies;
        this.warStars = warStars;
        this.attackWins = attackWins;
        this.defenseWins = defenseWins;
        this.builderHallLevel = builderHallLevel;
        this.builderBaseTrophies = builderBaseTrophies;
        this.bestBuilderBaseTrophies = bestBuilderBaseTrophies;
        this.role = role;
        this.warPreference = warPreference;
        this.donations = donations;
        this.donationsReceived = donationsReceived;
        this.clanCapitalContributions = clanCapitalContributions;
        this.clan = clan;
        this.leagueTier = leagueTier;
        this.builderBaseLeague = builderBaseLeague;
        this.currentLeagueGroupTag = currentLeagueGroupTag;
        this.currentLeagueSeasonId = currentLeagueSeasonId;
        this.previousLeagueGroupTag = previousLeagueGroupTag;
        this.previousLeagueSeasonId = previousLeagueSeasonId;
        this.labels = labels;
    }



}
