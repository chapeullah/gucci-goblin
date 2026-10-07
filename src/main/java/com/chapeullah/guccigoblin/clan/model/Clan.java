package com.chapeullah.guccigoblin.clan.model;

import com.chapeullah.guccigoblin.capitalleague.CapitalLeague;
import com.chapeullah.guccigoblin.leaguetier.LeagueTier;
import com.chapeullah.guccigoblin.location.Location;
import com.chapeullah.guccigoblin.player.model.Player;
import com.chapeullah.guccigoblin.warleague.WarLeague;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "clans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
            optional = true)
    @JoinColumn(
            name = "location_id",
            nullable = true)
    private Location location;

    @Column(name = "is_family_friendly",
            nullable = true)
    private Boolean isFamilyFriendly;

    @Embedded
    private ClanBadgeUrls clanBadgeUrls;

    @Column(name = "clan_level",
            nullable = true)
    private Integer clanLevel;

    @Column(name = "clan_points",
            nullable = true)
    private Integer clanPoints;

    @Column(name = "clan_builder_base_points",
            nullable = true)
    private Integer clanBuilderBasePoints;

    @Column(name = "clan_capital_points",
            nullable = true)
    private Integer clanCapitalPoints;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = true)
    @JoinColumn(
            name = "capital_league_id",
            nullable = true)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "war_league_id", nullable = true)
    private WarLeague warLeague;

    @Column(name = "members",
            nullable = true)
    private Integer members;

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(mappedBy = "clan")
    private List<Player> memberList = new ArrayList<>();

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            mappedBy = "clan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ClanLabelLink> labelLinks = new ArrayList<>();

    @Column(name = "required_builder_base_trophies",
            nullable = true)
    private Integer requiredBuilderBaseTrophies;

    @Column(name = "required_townhall_level",
            nullable = true)
    private Integer requiredTownhallLevel;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "required_league_tier_id", nullable = true)
    private LeagueTier requiredLeagueTier;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "chat_language_id", nullable = true)
    private ClanChatLanguage clanChatLanguage;

    public Clan(
            @NonNull String tag,
            @NonNull String name,
            @NonNull String type,
            String description,
            Location location,
            Boolean isFamilyFriendly,
            ClanBadgeUrls clanBadgeUrls,
            Integer clanLevel,
            Integer clanPoints,
            Integer clanBuilderBasePoints,
            Integer clanCapitalPoints,
            CapitalLeague capitalLeague,
            Integer requiredTrophies,
            String warFrequency,
            Integer warWinStreak,
            Integer warWins,
            Integer warTies,
            Integer warLosses,
            Boolean isWarLogPublic,
            WarLeague warLeague,
            Integer members,
            Integer requiredBuilderBaseTrophies,
            Integer requiredTownhallLevel,
            LeagueTier requiredLeagueTier,
            ClanChatLanguage clanChatLanguage) {
        this.tag = tag;
        this.name = name;
        this.type = type;
        this.description = description;
        this.location = location;
        this.isFamilyFriendly = isFamilyFriendly;
        this.clanBadgeUrls = clanBadgeUrls;
        this.clanLevel = clanLevel;
        this.clanPoints = clanPoints;
        this.clanBuilderBasePoints = clanBuilderBasePoints;
        this.clanCapitalPoints = clanCapitalPoints;
        this.capitalLeague = capitalLeague;
        this.requiredTrophies = requiredTrophies;
        this.warFrequency = warFrequency;
        this.warWinStreak = warWinStreak;
        this.warWins = warWins;
        this.warTies = warTies;
        this.warLosses = warLosses;
        this.isWarLogPublic = isWarLogPublic;
        this.warLeague = warLeague;
        this.members = members;
        this.requiredBuilderBaseTrophies = requiredBuilderBaseTrophies;
        this.requiredTownhallLevel = requiredTownhallLevel;
        this.requiredLeagueTier = requiredLeagueTier;
        this.clanChatLanguage = clanChatLanguage;
    }

    public void updateFrom(@NonNull Clan source) {
        if (!this.tag.equals(source.tag)) {
            throw new IllegalArgumentException("Clan tag mismatch");
        }
        this.name = source.name;
        this.type = source.type;
        this.description = source.description;
        this.location = source.location;
        this.isFamilyFriendly = source.isFamilyFriendly;
        this.clanBadgeUrls = source.clanBadgeUrls;
        this.clanLevel = source.clanLevel;
        this.clanPoints = source.clanPoints;
        this.clanBuilderBasePoints = source.clanBuilderBasePoints;
        this.clanCapitalPoints = source.clanCapitalPoints;
        this.capitalLeague = source.capitalLeague;
        this.requiredTrophies = source.requiredTrophies;
        this.warFrequency = source.warFrequency;
        this.warWinStreak = source.warWinStreak;
        this.warWins = source.warWins;
        this.warTies = source.warTies;
        this.warLosses = source.warLosses;
        this.isWarLogPublic = source.isWarLogPublic;
        this.warLeague = source.warLeague;
        this.members = source.members;
        this.requiredBuilderBaseTrophies = source.requiredBuilderBaseTrophies;
        this.requiredTownhallLevel = source.requiredTownhallLevel;
        this.requiredLeagueTier = source.requiredLeagueTier;
        this.clanChatLanguage = source.clanChatLanguage;
    }



}
