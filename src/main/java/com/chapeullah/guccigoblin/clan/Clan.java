package com.chapeullah.guccigoblin.clan;

import com.chapeullah.guccigoblin.capitalleague.CapitalLeague;
import com.chapeullah.guccigoblin.location.Location;
import com.chapeullah.guccigoblin.warleague.WarLeague;
import jakarta.persistence.*;

@Entity @Table(name = "clans")
public class Clan {

    @Id
    @Column(name = "tag",
            nullable = false)
    private String tag;

    @Column(name = "name",
            nullable = false)
    private String name;

    @Column(name = "type",
            nullable = false)
    private String type;

    @Column(name = "description",
            nullable = true)
    private String description;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(
            name = "location_id",
            nullable = false)
    private Location location;

    @Column(name = "is_family_friendly",
            nullable = false)
    private Boolean isFamilyFriendly;

    @Embedded
    private BadgeUrls badgeUrls;

    @Column(name = "clan_level",
            nullable = false)
    private Integer clanLevel;

    @Column(name = "clan_points",
            nullable = true)
    private Integer clanPoints;

    @Column(name = "clan_builder_base_points",
            nullable = true)
    private Integer clanBuilderBasePoints;

    @Column(name = "clan_capital_points",
            nullable = false)
    private Integer clanCapitalPoints;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(
            name = "capital_league_id",
            nullable = false)
    private CapitalLeague capitalLeague;

    @Column(name = "required_trophies",
            nullable = true)
    private Integer requiredTrophies;

    @Column(name = "war_frequency",
            nullable = true)
    private String warFrequency;

    @Column(name = "war_win_streak",
            nullable = true)
    private Integer warWinStreak;

    @Column(name = "war_wins",
            nullable = true)
    private Integer warWins;

    @Column(name = "war_ties",
            nullable = true)
    private Integer warTies;

    @Column(name = "war_losses",
            nullable = true)
    private Integer warLosses;

    @Column(name = "is_war_log_public",
            nullable = true)
    private Boolean isWarLogPublic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "war_league_id", nullable = false)
    private WarLeague warLeague;

    private Integer members;



}
