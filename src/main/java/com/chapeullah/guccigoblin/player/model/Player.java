package com.chapeullah.guccigoblin.player.model;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.clan.Clan;
import com.chapeullah.guccigoblin.label.player.PlayerLabel;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "players")
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

    @Column(name = "attacks_wins",
            nullable = false)
    private Integer attacksWins;

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
            nullable = false)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_label_id", nullable = false)
    private PlayerLabel playerLabel;

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

}
