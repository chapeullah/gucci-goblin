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

    private String name;
    private String type;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private Boolean isFamilyFriendly;

    @Embedded
    private BadgeUrls badgeUrls;

    private Integer clanLevel;
    private Integer clanPoints;
    private Integer clanBuilderBasePoints;
    private Integer clanCapitalPoints;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "capital_league_id", nullable = false)
    private CapitalLeague capitalLeague;

    private Integer requiredTrophies;
    private String warFrequency;
    private Integer warWinStreak;
    private Integer warWins;
    private Integer warTies;
    private Integer warLosses;
    private Boolean isWarLogPublic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "war_league_id", nullable = false)
    private WarLeague warLeague;

    private Integer members;



}
