package com.chapeullah.guccigoblin.player.model;

import com.chapeullah.guccigoblin.builderbaseleague.BuilderBaseLeague;
import com.chapeullah.guccigoblin.clan.Clan;
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
            nullable = true)
    private Integer townHallWeaponLevel;

    @Column(name = "exp_level",
            nullable = false)
    private Integer expLevel;

    @Column(name = "trophies",
            nullable = true)
    private Integer trophies;

    @Column(name = "best_trophies",
            nullable = true)
    private Integer bestTrophies;

    @Column(name = "war_stars",
            nullable = true)
    private Integer warStars;

    @Column(name = "attack_wins",
            nullable = true)
    private Integer attackWins;

    @Column(name = "defense_wins",
            nullable = true)
    private Integer defenseWins;

    @Column(name = "builder_hall_level",
            nullable = true)
    private Integer builderHallLevel;

    @Column(name = "builder_base_trophies",
            nullable = true)
    private Integer builderBaseTrophies;

    @Column(name = "best_builder_base_trophies",
            nullable = true)
    private Integer bestBuilderBaseTrophies;

    @Column(name = "role",
            nullable = true)
    private String role;

    @Column(name = "war_preference",
            nullable = true)
    private String warPreference;

    @Column(name = "donations",
            nullable = true)
    private Integer donations;

    @Column(name = "donations_received",
            nullable = true)
    private Integer donationsReceived;

    @Column(name = "clan_capital_contributions",
            nullable = true)
    private Integer clanCapitalContributions;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = true)
    @JoinColumn(
            name = "clan_tag",
            nullable = true)
    private Clan clan;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = true)
    @JoinColumn(
            name = "league_tier_id",
            nullable = true)
    private LeagueTier leagueTier;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = true)
    @JoinColumn(
            name = "builder_base_league_id",
            nullable = true)
    private BuilderBaseLeague builderBaseLeague;

    @Column(name = "current_league_group_tag",
            nullable = true)
    private String currentLeagueGroupTag;

    @Column(name = "current_league_season_id",
            nullable = true)
    private Integer currentLeagueSeasonId;

    @Column(name = "previous_league_group_tag",
            nullable = true)
    private String previousLeagueGroupTag;

    @Column(name = "previous_league_season_id",
            nullable = true)
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
    @OneToMany(
            mappedBy = "player",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<PlayerLabelLink> labelLinks = new ArrayList<>();

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
            Integer townHallWeaponLevel,
            @NonNull Integer expLevel,
            Integer trophies,
            Integer bestTrophies,
            Integer warStars,
            Integer attackWins,
            Integer defenseWins,
            Integer builderHallLevel,
            Integer builderBaseTrophies,
            Integer bestBuilderBaseTrophies,
            String role,
            String warPreference,
            Integer donations,
            Integer donationsReceived,
            Integer clanCapitalContributions,
            Clan clan,
            LeagueTier leagueTier,
            BuilderBaseLeague builderBaseLeague,
            String currentLeagueGroupTag,
            Integer currentLeagueSeasonId,
            String previousLeagueGroupTag,
            Integer previousLeagueSeasonId,
            List<Achievement> achievements,
            List<HouseElement> houseElements,
            List<PlayerLabelLink> labelLinks,
            List<Troop> troops,
            List<Hero> heroes,
            List<HeroEquipment> heroEquipments,
            List<Spell> spells) {
        if (!tag.matches("^#[A-Z0-9]+$")) {
            throw new IllegalArgumentException("Player tag must start with # followed by at least one uppercase letter or digit. Example: #ABC123");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
        if (townHallLevel < 1) {
            throw new IllegalArgumentException("Player town hall level must be at least 1");
        }
        if (townHallWeaponLevel != null && townHallWeaponLevel < 1) {
            throw new IllegalArgumentException("Player town hall weapon level must be at least 1");
        }
        if (expLevel < 1) {
            throw new IllegalArgumentException("Player exp level must be at least 1");
        }
        if (trophies != null && trophies < 0) {
            throw new IllegalArgumentException("Player trophies must not be negative");
        }
        if (bestTrophies != null && bestTrophies < 0) {
            throw new IllegalArgumentException("Player best trophies must not be negative");
        }
        if (warStars != null && warStars < 0) {
            throw new IllegalArgumentException("Player war stars must not be negative");
        }
        if (attackWins != null && attackWins < 0) {
            throw new IllegalArgumentException("Player attack wins must not be negative");
        }
        if (defenseWins != null && defenseWins < 0) {
            throw new IllegalArgumentException("Player defense wins must not be negative");
        }
        if (builderHallLevel != null && builderHallLevel < 1) {
            throw new IllegalArgumentException("Player builder hall level must be at least 1");
        }
        if (builderBaseTrophies != null && builderBaseTrophies < 0) {
            throw new IllegalArgumentException("Player builder base trophies must not be negative");
        }
        if (bestBuilderBaseTrophies != null && bestBuilderBaseTrophies < 0) {
            throw new IllegalArgumentException("Player best builder base trophies must not be negative");
        }
        if (role != null && role.isBlank()) {
            throw new IllegalArgumentException("Player role must not be blank");
        }
        if (warPreference != null && warPreference.isBlank()) {
            throw new IllegalArgumentException("Player war preference must not be blank");
        }
        if (donations != null && donations < 0) {
            throw new IllegalArgumentException("Player donations must not be negative");
        }
        if (donationsReceived != null && donationsReceived < 0) {
            throw new IllegalArgumentException("Player donations received must not be negative");
        }
        if (clanCapitalContributions != null && clanCapitalContributions < 0) {
            throw new IllegalArgumentException("Player clan capital contributions must not be negative");
        }
        if (currentLeagueGroupTag != null && currentLeagueGroupTag.isBlank()) {
            throw new IllegalArgumentException("Player current league group tag must not be blank");
        }
        if (previousLeagueGroupTag != null && previousLeagueGroupTag.isBlank()) {
            throw new IllegalArgumentException("Player previous league group tag must not be blank");
        }

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

        this.achievements = achievements;
        this.houseElements = houseElements;
        this.labelLinks = labelLinks;
        this.troops = troops;
        this.heroes = heroes;
        this.heroEquipments = heroEquipments;
        this.spells = spells;
    }

    public void updateFrom(Player source) {
        if (!this.tag.equals(source.tag)) {
            throw new IllegalArgumentException("Player tag mismatch");
        }

        this.name = source.name;
        this.townHallLevel = source.townHallLevel;
        this.townHallWeaponLevel = source.townHallWeaponLevel;
        this.expLevel = source.expLevel;
        this.trophies = source.trophies;
        this.bestTrophies = source.bestTrophies;
        this.warStars = source.warStars;
        this.attackWins = source.attackWins;
        this.defenseWins = source.defenseWins;
        this.builderHallLevel = source.builderHallLevel;
        this.builderBaseTrophies = source.builderBaseTrophies;
        this.bestBuilderBaseTrophies = source.bestBuilderBaseTrophies;
        this.role = source.role;
        this.warPreference = source.warPreference;
        this.donations = source.donations;
        this.donationsReceived = source.donationsReceived;
        this.clanCapitalContributions = source.clanCapitalContributions;

        this.clan = source.clan;
        this.leagueTier = source.leagueTier;
        this.builderBaseLeague = source.builderBaseLeague;

        this.currentLeagueGroupTag = source.currentLeagueGroupTag;
        this.currentLeagueSeasonId = source.currentLeagueSeasonId;
        this.previousLeagueGroupTag = source.previousLeagueGroupTag;
        this.previousLeagueSeasonId = source.previousLeagueSeasonId;
    }

}
